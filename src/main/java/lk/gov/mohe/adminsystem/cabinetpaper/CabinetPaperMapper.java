package lk.gov.mohe.adminsystem.cabinetpaper;

import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.*;
import org.springframework.lang.Nullable;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UserMapper.class})
public interface CabinetPaperMapper {
  @Mapping(source = "submittedByUser", target = "submittedByUser", qualifiedByName = "toUserDtoMin")
  CabinetPaperDto toDto(CabinetPaper cabinetPaper);

  CabinetPaperCategoryDto toCategoryDto(@Nullable CabinetPaperCategory category);

  // Map create request -> entity (category and submittedByUser handled in service)
  CabinetPaper toEntity(CreateCabinetPaperRequestDto request);
}
