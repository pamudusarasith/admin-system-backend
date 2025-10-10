package lk.gov.mohe.adminsystem.letter;

import java.util.*;
import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentParent;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {
  private final LetterRepository letterRepository;
  private final LetterEventRepository letterEventRepository;
  private final AttachmentRepository attachmentRepository;
  private final DivisionRepository divisionRepository;
  private final UserRepository userRepository;
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
      Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);
    if (authorities.contains("letter:all:read")) {
      return letterRepository.findAll(pageable).map(letterMapper::toLetterDtoMin);
    }

    Specification<Letter> spec = null;

    if (authorities.contains("letter:unassigned:read")) {
      spec =
          (root, query, cb) ->
              cb.and(cb.isNull(root.get("assignedDivision")), cb.isNull(root.get("assignedUser")));
    }

    if (authorities.contains("letter:division:read")) {
      Specification<Letter> divisionSpec =
          (root, query, cb) -> cb.equal(root.get("assignedDivision").get("id"), divisionId);
      spec = (spec == null) ? divisionSpec : spec.or(divisionSpec);
    }

    if (authorities.contains("letter:own:read")) {
      Specification<Letter> ownSpec =
          (root, query, cb) -> cb.equal(root.get("assignedUser").get("id"), userId);
      spec = (spec == null) ? ownSpec : spec.or(ownSpec);
    }

    if (spec == null) {
      // No permitted scope matched: return empty page to avoid unintended full
      // access
      log.warn(
          "User [{}] with authorities {} attempted to access letters with no permitted scope.",
          userId,
          authorities);
      return Page.empty(pageable);
    }

    return letterRepository.findAll(spec, pageable).map(letterMapper::toLetterDtoMin);
  }

  public LetterDto getLetterById(
      Integer id, Integer userId, Integer divisionId, Collection<String> authorities) {
    Letter letter =
        letterRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (!hasAccessToLetter(letter, userId, divisionId, authorities, "read")) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You do not have permission to access this letter");
    }

    List<Attachment> attachments =
        attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.LETTER, letter.getId());
    List<LetterEvent> events = letterEventRepository.findByLetterId(letter.getId());
    events.forEach(
        event -> {
          Map<String, Object> details = event.getEventDetails();
          if (details != null) {
            event.setEventDetails(populateEventDetails(details));
          }
        });

    return letterMapper.toLetterDtoFull(letter, attachments, events);
  }

  @Transactional
  public Letter createLetter(CreateOrUpdateLetterRequestDto request, MultipartFile[] attachments) {
    if (letterRepository.existsLetterByReference(request.reference())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Reference already exists");
    }

    Letter letter = letterMapper.toEntity(request);
    letter.setStatus(StatusEnum.NEW);
    Letter savedLetter = letterRepository.save(letter);

    saveAttachments(savedLetter, attachments);

    Map<String, Object> eventDetails = Map.of("newStatus", savedLetter.getStatus().toString());
    createLetterEvent(savedLetter, EventTypeEnum.CHANGE_STATUS, eventDetails);

    return savedLetter;
  }

  public void updateLetter(
      Integer id,
      CreateOrUpdateLetterRequestDto request,
      Integer userId,
      Integer divisionId,
      Collection<String> authorities) {
    Letter letter =
        letterRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (!hasAccessToLetter(letter, userId, divisionId, authorities, "update")) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You do not have permission to update this letter");
    }

    letterMapper.updateEntityFromCreateOrUpdateLetterRequestDto(request, letter);

    letterRepository.save(letter);
  }

  @Transactional
  public void assignDivision(Integer letterId, Integer divisionId) {
    Letter letter =
        letterRepository
            .findById(letterId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (letter.getAssignedDivision() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Letter is already assigned to a division");
    }

    Division division =
        divisionRepository
            .findById(divisionId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));

    letter.setStatus(StatusEnum.ASSIGNED_TO_DIVISION);
    letter.setAssignedDivision(division);
    letter.setAssignedUser(null);
    letterRepository.save(letter);

    Map<String, Object> eventDetails =
        Map.of("newStatus", StatusEnum.ASSIGNED_TO_DIVISION, "assignedDivisionId", divisionId);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
  }

  @Transactional
  public void assignUser(Integer letterId, Integer userId) {
    Letter letter =
        letterRepository
            .findById(letterId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (letter.getAssignedDivision() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Letter must be assigned to a division before assigning to a user");
    }

    if (letter.getAssignedUser() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Letter is already assigned to a user");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    letter.setStatus(StatusEnum.PENDING_ACCEPTANCE);
    letter.setAssignedUser(user);
    letterRepository.save(letter);

    Map<String, Object> eventDetails =
        Map.of("newStatus", StatusEnum.PENDING_ACCEPTANCE, "assignedUserId", userId);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
  }

  @Transactional
  public void addNote(
      Integer letterId,
      String content,
      MultipartFile[] attachments,
      Integer userId,
      Integer divisionId,
      Collection<String> authorities) {
    Letter letter =
        letterRepository
            .findById(letterId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (!hasAccessToLetter(letter, userId, divisionId, authorities, "add:note")) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You do not have permission to add a note to this letter");
    }

    Map<String, Object> eventDetails = new HashMap<>();
    eventDetails.put("content", content);

    LetterEvent letterEvent = createLetterEvent(letter, EventTypeEnum.ADD_NOTE, eventDetails);

    List<Attachment> savedAttachments = saveAttachments(letterEvent, attachments);
    List<Integer> attachmentIds = savedAttachments.stream().map(Attachment::getId).toList();
    eventDetails.put("attachmentIds", attachmentIds);
    letterEvent.setEventDetails(eventDetails);
    letterEventRepository.save(letterEvent);
  }

  private LetterEvent createLetterEvent(
      Letter letter, EventTypeEnum eventType, Map<String, Object> eventDetails) {
    LetterEvent letterEvent = new LetterEvent();
    letterEvent.setLetter(letter);

    User user = currentUserProvider.getCurrentUserOrThrow();
    letterEvent.setUser(user);

    letterEvent.setEventType(eventType);
    letterEvent.setEventDetails(eventDetails);
    return letterEventRepository.save(letterEvent);
  }

  private List<Attachment> saveAttachments(AttachmentParent parent, MultipartFile[] files) {
    List<Attachment> attachmentList = new ArrayList<>();
    if (files != null) {
      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the files is empty");
        }
        if (!acceptedMimeTypes.contains(file.getContentType())) {
          throw new ResponseStatusException(
              HttpStatus.UNSUPPORTED_MEDIA_TYPE,
              "Attachment type " + file.getContentType() + " is not supported");
        }
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        String folder =
            switch (parent.getType()) {
              case ParentTypeEnum.LETTER -> "letters";
              case ParentTypeEnum.LETTER_EVENT -> "events";
            };
        String objectName = storageService.upload(folder, file);
        attachment.setFilePath(objectName);
        attachment.setFileType(file.getContentType());
        attachment.attachToParent(parent);
        attachment = attachmentRepository.save(attachment);
        attachmentList.add(attachment);
      }
    }
    return attachmentList;
  }

  private boolean hasAccessToLetter(
      Letter letter,
      Integer userId,
      Integer divisionId,
      Collection<String> authorities,
      String action) {
    boolean hasAllAccess = authorities.contains("letter:all:" + action);
    boolean hasUnassignedAccess =
        authorities.contains("letter:unassigned:" + action)
            && letter.getAssignedDivision() == null
            && letter.getAssignedUser() == null;
    boolean hasDivisionAccess =
        authorities.contains("letter:division:" + action)
            && letter.getAssignedDivision() != null
            && letter.getAssignedDivision().getId().equals(divisionId);
    boolean hasOwnAccess =
        authorities.contains("letter:own:" + action)
            && letter.getAssignedUser() != null
            && letter.getAssignedUser().getId().equals(userId);
    return hasAllAccess || hasUnassignedAccess || hasDivisionAccess || hasOwnAccess;
  }

  private Map<String, Object> populateEventDetails(Map<String, Object> eventDetails) {
    Map<String, Object> eventDetailsMap = new HashMap<>();
    for (Map.Entry<String, Object> entry : eventDetails.entrySet()) {
      switch (entry.getKey()) {
        case "attachmentIds" -> {
          @SuppressWarnings("unchecked")
          List<Integer> attachmentIds = (List<Integer>) entry.getValue();
          List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);
          eventDetailsMap.put("attachments", attachments);
        }
        case "assignedDivisionId" -> {
          Integer divisionId = (Integer) entry.getValue();
          Division division =
              divisionRepository
                  .findById(divisionId)
                  .orElseThrow(
                      () ->
                          new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
          eventDetailsMap.put("assignedDivision", division);
        }
        case "assignedUserId" -> {
          Integer userId = (Integer) entry.getValue();
          User user =
              userRepository
                  .findById(userId)
                  .orElseThrow(
                      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
          eventDetailsMap.put("assignedUser", user);
        }
        default -> eventDetailsMap.put(entry.getKey(), entry.getValue());
      }
    }
    return eventDetailsMap;
  }
}
