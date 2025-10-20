package lk.gov.mohe.adminsystem.cabinetpaper.category;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CabinetPaperCategoryMapper {
  CabinetPaperCategoryDto toDto(CabinetPaperCategory category);

  CabinetPaperCategory toEntity(CreateCabinetPaperCategoryRequestDto request);

  void updateEntityFromDto(
      UpdateCabinetPaperCategoryRequestDto request, @MappingTarget CabinetPaperCategory category);
}
