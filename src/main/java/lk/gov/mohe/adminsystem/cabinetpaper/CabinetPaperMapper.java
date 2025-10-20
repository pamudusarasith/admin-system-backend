package lk.gov.mohe.adminsystem.cabinetpaper;

import java.util.List;
import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentMapper;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryDto;
import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UserMapper.class, AttachmentMapper.class})
public abstract class CabinetPaperMapper {
  @Autowired protected AttachmentRepository attachmentRepository;
  ParentTypeEnum cabinetPaperParentType = ParentTypeEnum.CABINET_PAPER;

  @Mapping(source = "submittedByUser", target = "submittedByUser", qualifiedByName = "toUserDtoMin")
  @Mapping(
      target = "noOfAttachments",
      expression =
          "java( attachmentRepository.countByParentIdAndParentType(cabinetPaper.getId(), cabinetPaperParentType) )")
  @Mapping(target = "attachments", ignore = true)
  public abstract CabinetPaperDto toCabinetPaperDtoMin(CabinetPaper cabinetPaper);

  @Mapping(
      source = "cabinetPaper.submittedByUser",
      target = "submittedByUser",
      qualifiedByName = "toUserDtoMin")
  @Mapping(target = "noOfAttachments", ignore = true)
  public abstract CabinetPaperDto toCabinetPaperDtoFull(
      CabinetPaper cabinetPaper, List<Attachment> attachments);

  public abstract CabinetPaperCategoryDto toCategoryDto(@Nullable CabinetPaperCategory category);

  // Map create request -> entity (category and submittedByUser handled in service)
  public abstract CabinetPaper toEntity(CreateCabinetPaperRequestDto request);
}
