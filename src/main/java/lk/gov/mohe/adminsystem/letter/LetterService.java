package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentParent;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {
    private final LetterRepository letterRepository;
    private final LetterEventRepository letterEventRepository;
    private final AttachmentRepository attachmentRepository;
    private final LetterMapper letterMapper;
    private final MinioStorageService storageService;
    private final CurrentUserProvider currentUserProvider;

    @Value("${custom.attachments.accepted-mime-types}")
    private final Set<String> acceptedMimeTypes;

    public Page<LetterDto> getAccessibleLetters(
        Integer userId,
        Integer divisionId,
        Collection<String> authorities,
        Integer page,
        Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        if (authorities.contains("letter:all:read")) {
            return letterRepository.findAll(pageable).map(letterMapper::toLetterDtoMin);
        }

        Specification<Letter> spec = null;

        if (authorities.contains("letter:unassigned:read")) {
            spec = (root, query, cb) ->
                cb.and(
                    cb.isNull(root.get("assignedDivision")),
                    cb.isNull(root.get("assignedUser"))
                );
        }

        if (authorities.contains("letter:division:read")) {
            Specification<Letter> divisionSpec = (root, query, cb) ->
                cb.equal(root.get("assignedDivision").get("id"), divisionId);
            spec = (spec == null) ? divisionSpec : spec.or(divisionSpec);
        }

        if (authorities.contains("letter:own:read")) {
            Specification<Letter> ownSpec = (root, query, cb) ->
                cb.equal(root.get("assignedUser").get("id"), userId);
            spec = (spec == null) ? ownSpec : spec.or(ownSpec);
        }

        if (spec == null) {
            // No permitted scope matched: return empty page to avoid unintended full
            // access
            log.warn("User [{}] with authorities {} attempted to access letters with no" +
                " permitted scope.", userId, authorities);
            return Page.empty(pageable);
        }

        return letterRepository.findAll(spec, pageable).map(letterMapper::toLetterDtoMin);
    }

    public LetterDto getLetterById(
        Integer id,
        Integer userId,
        Integer divisionId,
        Collection<String> authorities
    ) {
        Letter letter = letterRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Letter not found with id: " + id));

        if (!hasAccessToLetter(letter, userId, divisionId, authorities, "read")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You do not have permission to access this letter");
        }

        List<Attachment> attachments =
            attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.LETTER,
                letter.getId());
        List<LetterEvent> events = letterEventRepository.findByLetterId(letter.getId());

        // Collect all event IDs for ADD_NOTE events
        List<Integer> addNoteEventIds = events.stream()
            .filter(e -> e.getEventType() == EventTypeEnum.ADD_NOTE)
            .map(LetterEvent::getId)
            .toList();

        // Fetch all attachments for these event IDs in one query
        List<Attachment> allEventAttachments = addNoteEventIds.isEmpty() ? List.of() :
            attachmentRepository.findByParentTypeAndParentIdIn(ParentTypeEnum.LETTER_EVENT, addNoteEventIds);

        // Map event ID to its attachments
        Map<Integer, List<Attachment>> attachmentsByEventId = allEventAttachments.stream()
            .collect(Collectors.groupingBy(Attachment::getParentId));

        for (LetterEvent event : events) {
            if (event.getEventType() != EventTypeEnum.ADD_NOTE)
                continue;
            List<Attachment> eventAttachments =
                attachmentsByEventId.getOrDefault(event.getId(), List.of());
            Map<String, Object> details = event.getEventDetails();
            details.put("attachments", eventAttachments);
            event.setEventDetails(details);
        }

        return letterMapper.toLetterDtoFull(letter, attachments, events);
    }

    @Transactional
    public Letter createLetter(
        CreateOrUpdateLetterRequestDto request,
        MultipartFile[] attachments
    ) {
        if (letterRepository.existsLetterByReference(request.reference())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reference already exists");
        }

        Letter letter = letterMapper.toEntity(request);
        letter.setStatus(StatusEnum.NEW);
        Letter savedLetter = letterRepository.save(letter);

        saveAttachments(savedLetter, attachments);

        Map<String, Object> eventDetails = Map.of(
            "newStatus", savedLetter.getStatus().toString()
        );
        createLetterEvent(savedLetter, EventTypeEnum.CHANGE_STATUS, eventDetails);

        return savedLetter;
    }

    public void updateLetter(
        Integer id,
        CreateOrUpdateLetterRequestDto request,
        Integer userId,
        Integer divisionId,
        Collection<String> authorities
    ) {
        Letter letter = letterRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Letter not found with id: " + id));

        if (!hasAccessToLetter(letter, userId, divisionId, authorities, "update")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You do not have permission to update this letter");
        }

        letterMapper.updateEntityFromCreateOrUpdateLetterRequestDto(request, letter);

        letterRepository.save(letter);
    }

    @Transactional
    public void addNote(
        Integer letterId,
        String content,
        MultipartFile[] attachments,
        Integer userId,
        Integer divisionId,
        Collection<String> authorities
    ) {
        Letter letter = letterRepository.findById(letterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Letter not found with id: " + letterId));

        if (!hasAccessToLetter(letter, userId, divisionId, authorities, "add:note")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You do not have permission to add a note to this letter");
        }

        Map<String, Object> eventDetails = Map.of(
            "content", content
        );

        LetterEvent letterEvent = createLetterEvent(letter, EventTypeEnum.ADD_NOTE,
            eventDetails);

        saveAttachments(letterEvent, attachments);
    }

    private LetterEvent createLetterEvent(
        Letter letter, EventTypeEnum eventType,
        Map<String, Object> eventDetails
    ) {
        LetterEvent letterEvent = new LetterEvent();
        letterEvent.setLetter(letter);

        User user = currentUserProvider.getCurrentUserOrThrow();
        letterEvent.setUser(user);

        letterEvent.setEventType(eventType);
        letterEvent.setEventDetails(eventDetails);
        return letterEventRepository.save(letterEvent);
    }

    private void saveAttachments(
        AttachmentParent parent, MultipartFile[] attachments
    ) {
        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (attachment.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "One of the attachments is empty");
                }
                if (!acceptedMimeTypes.contains(attachment.getContentType())) {
                    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Attachment type " + attachment.getContentType() + " is not " +
                            "supported");
                }
                Attachment newAttachment = new Attachment();
                newAttachment.setFileName(attachment.getOriginalFilename());
                String folder = switch (parent.getType()) {
                    case ParentTypeEnum.LETTER -> "letters";
                    case ParentTypeEnum.LETTER_EVENT -> "events";
                };
                String objectName = storageService.upload(folder, attachment);
                newAttachment.setFilePath(objectName);
                newAttachment.setFileType(attachment.getContentType());
                newAttachment.attachToParent(parent);
                attachmentRepository.save(newAttachment);
            }
        }
    }

    private boolean hasAccessToLetter(
        Letter letter,
        Integer userId,
        Integer divisionId,
        Collection<String> authorities,
        String action
    ) {
        boolean hasAllAccess = authorities.contains("letter:all:" + action);
        boolean hasUnassignedAccess = authorities.contains("letter:unassigned:" + action)
            && letter.getAssignedDivision() == null
            && letter.getAssignedUser() == null;
        boolean hasDivisionAccess = authorities.contains("letter:division:" + action)
            && letter.getAssignedDivision() != null
            && letter.getAssignedDivision().getId().equals(divisionId);
        boolean hasOwnAccess = authorities.contains("letter:own:" + action)
            && letter.getAssignedUser() != null
            && letter.getAssignedUser().getId().equals(userId);
        return hasAllAccess || hasUnassignedAccess || hasDivisionAccess || hasOwnAccess;
    }
}
