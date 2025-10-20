package lk.gov.mohe.adminsystem.cabinetpaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CabinetPaperService {
  private static final String CABINET_PAPER_NOT_FOUND = "Cabinet paper not found with id: ";
  
  private final CabinetPaperRepository cabinetPaperRepository;
  private final CabinetPaperCategoryRepository categoryRepository;
  private final AttachmentRepository attachmentRepository;
  private final MinioStorageService storageService;
  private final CurrentUserProvider currentUserProvider;
  private final CabinetPaperMapper cabinetPaperMapper;

  @Value("${custom.attachments.accepted-mime-types}")
  private final Set<String> acceptedMimeTypes;

  @Transactional(readOnly = true)
  public Page<CabinetPaperDto> getCabinetPapers(Integer page, Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);
    Page<CabinetPaper> cabinetPapers = cabinetPaperRepository.findAll(pageable);
    return cabinetPapers.map(cabinetPaperMapper::toCabinetPaperDtoMin);
  }

  @Transactional(readOnly = true)
  public CabinetPaperDto getCabinetPaperById(Integer id) {
    CabinetPaper cabinetPaper =
        cabinetPaperRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, CABINET_PAPER_NOT_FOUND + id));

    List<Attachment> attachments =
        attachmentRepository.findByParentTypeAndParentId(
            ParentTypeEnum.CABINET_PAPER, cabinetPaper.getId());

    return cabinetPaperMapper.toCabinetPaperDtoFull(cabinetPaper, attachments);
  }

  @Transactional
  public CabinetPaper createCabinetPaper(
      CreateCabinetPaperRequestDto request, MultipartFile[] attachments) {
    if (cabinetPaperRepository.existsByReferenceId(request.referenceId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Reference ID already exists");
    }

    CabinetPaper cabinetPaper = cabinetPaperMapper.toEntity(request);

    CabinetPaperCategory category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    cabinetPaper.setCategory(category);

    User currentUser = currentUserProvider.getCurrentUserOrThrow();
    cabinetPaper.setSubmittedByUser(currentUser);

    CabinetPaper savedCabinetPaper = cabinetPaperRepository.save(cabinetPaper);

    saveAttachments(savedCabinetPaper, attachments);

    return savedCabinetPaper;
  }

  @Transactional
  public CabinetPaper updateCabinetPaper(
      Integer id, UpdateCabinetPaperRequestDto request, MultipartFile[] newAttachments) {
    CabinetPaper cabinetPaper =
        cabinetPaperRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, CABINET_PAPER_NOT_FOUND + id));

    // Check if reference ID is being changed and if new reference ID already exists
    if (!cabinetPaper.getReferenceId().equals(request.referenceId())
        && cabinetPaperRepository.existsByReferenceId(request.referenceId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Reference ID '" + request.referenceId() + "' already exists");
    }

    // Update basic fields
    cabinetPaperMapper.updateEntityFromDto(request, cabinetPaper);

    // Update category if changed
    if (!cabinetPaper.getCategory().getId().equals(request.categoryId())) {
      CabinetPaperCategory category =
          categoryRepository
              .findById(request.categoryId())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
      cabinetPaper.setCategory(category);
    }

    CabinetPaper updatedCabinetPaper = cabinetPaperRepository.save(cabinetPaper);

    // Save new attachments if provided
    if (newAttachments != null && newAttachments.length > 0) {
      saveAttachments(updatedCabinetPaper, newAttachments);
    }

    return updatedCabinetPaper;
  }

  @Transactional
  public void deleteCabinetPaper(Integer id) {
    CabinetPaper cabinetPaper =
        cabinetPaperRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, CABINET_PAPER_NOT_FOUND + id));

    // Get all attachments and delete from storage
    List<Attachment> attachments =
        attachmentRepository.findByParentTypeAndParentId(
            ParentTypeEnum.CABINET_PAPER, cabinetPaper.getId());

    for (Attachment attachment : attachments) {
      try {
        storageService.delete(attachment.getFilePath());
      } catch (Exception e) {
        // Log error but continue with deletion
        log.error("Failed to delete file: {}", attachment.getFilePath(), e);
      }
    }

    // Delete attachments from database
    attachmentRepository.deleteAll(attachments);

    // Delete cabinet paper
    cabinetPaperRepository.delete(cabinetPaper);
  }

  private List<Attachment> saveAttachments(CabinetPaper cabinetPaper, MultipartFile[] files) {
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
        String objectName = storageService.upload("cabinet-papers", file);
        attachment.setFilePath(objectName);
        attachment.setFileType(file.getContentType());
        attachment.attachToParent(cabinetPaper);
        attachment = attachmentRepository.save(attachment);
        attachmentList.add(attachment);
      }
    }
    return attachmentList;
  }
}
