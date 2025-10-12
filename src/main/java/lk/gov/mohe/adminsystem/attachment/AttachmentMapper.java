package lk.gov.mohe.adminsystem.attachment;

import java.util.List;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AttachmentMapper {
  @Autowired MinioStorageService minioStorageService;

  @Mapping(
      target = "url",
      expression = "java( minioStorageService.getFileUrl(attachment.getFilePath()) )")
  public abstract AttachmentDto toDto(Attachment attachment);

  public abstract List<AttachmentDto> toDtoList(List<Attachment> attachments);
}
