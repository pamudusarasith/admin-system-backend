package lk.gov.mohe.adminsystem.letter;

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
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static lk.gov.mohe.adminsystem.letter.LetterSpecs.*;
import static lk.gov.mohe.adminsystem.util.SpecificationsUtil.andSpec;
import static lk.gov.mohe.adminsystem.util.SpecificationsUtil.orSpec;

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

  @Transactional(readOnly = true)
  public Page<LetterDto> getAccessibleLetters(
      Integer userId,
      Integer divisionId,
      Collection<String> authorities,
      LetterSearchParams filters,
      Integer page,
      Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);
    Specification<Letter> filterSpec = buildFilterSpec(filters);

    if (authorities.contains("letter:all:read")) {
      return findLetters(filterSpec, pageable);
    }

    Specification<Letter> scopeSpec = buildScopeSpec(authorities, divisionId, userId);

    if (scopeSpec == null) {
      // No permitted scope matched: return empty page to avoid unintended full
      // access
      log.warn(
          "User [{}] with authorities {} attempted to access letters with no permitted scope.",
          userId,
          authorities);
      return Page.empty(pageable);
    }

    Specification<Letter> finalSpec = andSpec(scopeSpec, filterSpec);
    return findLetters(finalSpec, pageable);
  }

  private Specification<Letter> buildFilterSpec(LetterSearchParams filters) {
    if (filters == null) {
      return null;
    }

    Specification<Letter> spec = null;

    spec = withText(spec, filters.getQuery(), LetterSpecs::matchesQuery);
    spec = withValue(spec, filters.getStatus(), LetterSpecs::hasStatus);
    spec = withValue(spec, filters.getPriority(), LetterSpecs::hasPriority);
    spec = withValue(spec, filters.getModeOfArrival(), LetterSpecs::hasModeOfArrival);
    spec = withText(spec, filters.getSender(), LetterSpecs::hasSenderContaining);
    spec = withText(spec, filters.getReceiver(), LetterSpecs::hasReceiverContaining);

    spec = applyDateFilters(
        spec,
        filters.getSentDate(),
        filters.getSentDateFrom(),
        filters.getSentDateTo(),
        LetterSpecs::hasSentDate,
        LetterSpecs::hasSentDateOnOrAfter,
        LetterSpecs::hasSentDateOnOrBefore);

    spec = applyDateFilters(
        spec,
        filters.getReceivedDate(),
        filters.getReceivedDateFrom(),
        filters.getReceivedDateTo(),
        LetterSpecs::hasReceivedDate,
        LetterSpecs::hasReceivedDateOnOrAfter,
        LetterSpecs::hasReceivedDateOnOrBefore);

    spec = withText(
        spec, filters.getAssignedDivision(), LetterSpecs::hasAssignedDivisionNameContaining);
    spec = withText(spec, filters.getAssignedUser(), LetterSpecs::hasAssignedUserContaining);

    return spec;
  }

  private Specification<Letter> buildScopeSpec(
      Collection<String> authorities, Integer divisionId, Integer userId) {
    Specification<Letter> spec = null;

    if (authorities.contains("letter:unassigned:read")) {
      spec = orSpec(spec, hasNoAssignment());
    }

    if (authorities.contains("letter:division:read")) {
      spec = orSpec(spec, belongsToDivision(divisionId));
    }

    if (authorities.contains("letter:own:read")) {
      spec = orSpec(spec, assignedToUser(userId));
    }

    return spec;
  }

  private Page<LetterDto> findLetters(Specification<Letter> spec, Pageable pageable) {
    Page<Letter> letters = (spec == null)
        ? letterRepository.findAll(pageable)
        : letterRepository.findAll(spec, pageable);

    return letters.map(letterMapper::toLetterDtoMin);
  }

  private Specification<Letter> withText(
      Specification<Letter> base, String value, Function<String, Specification<Letter>> mapper) {
    return StringUtils.hasText(value) ? andSpec(base, mapper.apply(value)) : base;
  }

  private <T> Specification<Letter> withValue(
      Specification<Letter> base, T value, Function<T, Specification<Letter>> mapper) {
    return value != null ? andSpec(base, mapper.apply(value)) : base;
  }

  private Specification<Letter> applyDateFilters(
      Specification<Letter> base,
      LocalDate exact,
      LocalDate from,
      LocalDate to,
      Function<LocalDate, Specification<Letter>> exactSpec,
      Function<LocalDate, Specification<Letter>> fromSpec,
      Function<LocalDate, Specification<Letter>> toSpec) {
    Specification<Letter> spec = base;

    if (exact != null) {
      return andSpec(spec, exactSpec.apply(exact));
    }

    if (from != null) {
      spec = andSpec(spec, fromSpec.apply(from));
    }

    if (to != null) {
      spec = andSpec(spec, toSpec.apply(to));
    }

    return spec;
  }

  @Transactional(readOnly = true)
  public LetterDto getLetterById(
      Integer id, Integer userId, Integer divisionId, Collection<String> authorities) {
    Letter letter = letterRepository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (!hasAccessToLetter(letter, userId, divisionId, authorities, "read")) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You do not have permission to access this letter");
    }

    List<Attachment> attachments = attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.LETTER,
        letter.getId());
    List<LetterEvent> events = letterEventRepository.findByLetterIdOrderByCreatedAtDesc(letter.getId());
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

  @Transactional
  public void updateLetter(
      Integer id,
      CreateOrUpdateLetterRequestDto request,
      Integer userId,
      Integer divisionId,
      Collection<String> authorities) {
    Letter letter = letterRepository
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
    Letter letter = letterRepository
        .findById(letterId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (letter.getAssignedDivision() != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Letter is already assigned to a division");
    }

    Division division = divisionRepository
        .findById(divisionId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));

    letter.setStatus(StatusEnum.ASSIGNED_TO_DIVISION);
    letter.setAssignedDivision(division);
    letter.setAssignedUser(null);
    letterRepository.save(letter);

    Map<String, Object> eventDetails = Map.of("newStatus", StatusEnum.ASSIGNED_TO_DIVISION, "divisionId", divisionId);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
  }

  @Transactional
  public void assignUser(Integer letterId, Integer userId) {
    Letter letter = letterRepository
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

    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (!user.getDivision().getId().equals(letter.getAssignedDivision().getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "User must belong to the division assigned to the letter");
    }

    letter.setStatus(StatusEnum.PENDING_ACCEPTANCE);
    letter.setAssignedUser(user);
    letterRepository.save(letter);

    Map<String, Object> eventDetails = Map.of("newStatus", StatusEnum.PENDING_ACCEPTANCE, "userId", userId);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
  }

  @Transactional
  public void returnFromUser(Integer letterId, Integer currentUserId, String reason) {
    Letter letter = letterRepository
        .findById(letterId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));
    if (letter.getAssignedUser() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Letter is not assigned to any user");
    }
    if (!letter.getAssignedUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You can only return letters assigned to you");
    }
    letter.setStatus(StatusEnum.RETURNED_FROM_OFFICER);
    letter.setAssignedUser(null);
    letterRepository.save(letter);
    Map<String, Object> eventDetails = Map.of("newStatus", StatusEnum.RETURNED_FROM_OFFICER, "userId", currentUserId,
        "reason",
        reason);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
  }

  @Transactional
  public void returnFromDivision(
      Integer letterId, Integer currentUserDivisionId, ReturnRequestDto dto) {
    Letter letter = letterRepository
        .findById(letterId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (letter.getAssignedUser() != null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Letter is assigned to an officer, unassign the officer first");
    }

    if (letter.getAssignedDivision() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Letter is not assigned to any division");
    }

    if (!letter.getAssignedDivision().getId().equals(currentUserDivisionId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You can only unassign letters from your own division");
    }

    letter.setStatus(StatusEnum.RETURNED_FROM_DIVISION);
    letter.setAssignedDivision(null);
    letterRepository.save(letter);

    Map<String, Object> eventDetails = Map.of(
        "newStatus",
        StatusEnum.RETURNED_FROM_DIVISION,
        "divisionId",
        currentUserDivisionId,
        "reason",
        dto.reason());
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
    Letter letter = letterRepository
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

  @Transactional
  public void acceptLetter(Integer letterId, Integer userId) {
    Letter letter = letterRepository
        .findById(letterId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Letter not found"));

    if (letter.getAssignedUser() == null || !letter.getAssignedUser().getId().equals(userId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You can only accept letters assigned to you");
    }

    if (letter.getStatus() != StatusEnum.PENDING_ACCEPTANCE) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Letter must be in pending acceptance status to be accepted");
    }

    letter.setStatus(StatusEnum.ASSIGNED_TO_OFFICER);
    letterRepository.save(letter);

    Map<String, Object> eventDetails = Map.of("newStatus", StatusEnum.ASSIGNED_TO_OFFICER);
    createLetterEvent(letter, EventTypeEnum.CHANGE_STATUS, eventDetails);
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
        String folder = switch (parent.getType()) {
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
        case "divisionId" -> {
          Integer divisionId = (Integer) entry.getValue();
          Division division = divisionRepository
              .findById(divisionId)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
          division = Hibernate.unproxy(division, Division.class);
          eventDetailsMap.put("division", division);
        }
        case "userId" -> {
          Integer userId = (Integer) entry.getValue();
          User user = userRepository
              .findById(userId)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
          user = Hibernate.unproxy(user, User.class);
          eventDetailsMap.put("user", user);
        }
        default -> eventDetailsMap.put(entry.getKey(), entry.getValue());
      }
    }
    return eventDetailsMap;
  }
}
