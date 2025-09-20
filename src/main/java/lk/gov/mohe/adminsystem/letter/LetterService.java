package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Page<LetterDto> getAccessibleLetters(
        Integer userId,
        Integer divisionId,
        Collection<String> authorities,
        Integer page,
        Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        if (authorities.contains("letter:read:all")) {
            return letterRepository.findAll(pageable).map(letterMapper::toLetterDtoMin);
        }

        Specification<Letter> spec = null;

        if (authorities.contains("letter:read:unassigned")) {
            spec = (root, query, cb) ->
                cb.and(
                    cb.isNull(root.get("assignedDivision")),
                    cb.isNull(root.get("assignedUser"))
                );
        }

        if (authorities.contains("letter:read:division")) {
            Specification<Letter> divisionSpec = (root, query, cb) ->
                cb.equal(root.get("assignedDivision").get("id"), divisionId);
            spec = (spec == null) ? divisionSpec : spec.or(divisionSpec);
        }

        if (authorities.contains("letter:read:own")) {
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

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (attachment.isEmpty()) {
                    throw new IllegalArgumentException("Attachment cannot be empty");
                }
                Attachment newAttachment = new Attachment();
                newAttachment.setFileName(attachment.getOriginalFilename());
                String objectName = storageService.upload("letters", attachment);
                newAttachment.setFilePath(objectName);
                newAttachment.setFileType(attachment.getContentType());
                newAttachment.attachToParent(savedLetter);
                attachmentRepository.save(newAttachment);
            }
        }

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
            .orElseThrow(() -> new IllegalArgumentException("Letter not found with id: "
                + id));

        if (!hasAccessToLetter(letter, userId, divisionId, authorities, "update")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You do not have permission to update this letter");
        }

        letterMapper.updateEntityFromCreateOrUpdateLetterRequestDto(request, letter);

        letterRepository.save(letter);
    }

    private void createLetterEvent(
        Letter letter, EventTypeEnum eventType,
        Map<String, Object> eventDetails
    ) {
        LetterEvent letterEvent = new LetterEvent();
        letterEvent.setLetter(letter);

        User user = currentUserProvider.getCurrentUserOrThrow();
        letterEvent.setUser(user);

        letterEvent.setEventType(eventType);
        letterEvent.setEventDetails(eventDetails);
        letterEventRepository.save(letterEvent);
    }

    private boolean hasAccessToLetter(
        Letter letter,
        Integer userId,
        Integer divisionId,
        Collection<String> authorities,
        String action
    ) {
        boolean hasAllAccess = authorities.contains("letter:" + action + ":all");
        boolean hasUnassignedAccess = authorities.contains("letter:" + action +
            ":unassigned")
            && letter.getAssignedDivision() == null
            && letter.getAssignedUser() == null;
        boolean hasDivisionAccess = authorities.contains("letter:" + action + ":division")
            && letter.getAssignedDivision() != null
            && letter.getAssignedDivision().getId().equals(divisionId);
        boolean hasOwnAccess = authorities.contains("letter:" + action + ":own")
            && letter.getAssignedUser() != null
            && letter.getAssignedUser().getId().equals(userId);
        return hasAllAccess || hasUnassignedAccess || hasDivisionAccess || hasOwnAccess;
    }
}
