package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UserMapper.class})
public interface CabinetDecisionMapper {
  @Mapping(source = "paper.id", target = "paperId")
  @Mapping(source = "recordedByUser", target = "recordedByUser", qualifiedByName = "toUserDtoMin")
  CabinetDecisionDto toDto(CabinetDecision cabinetDecision);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "paper", ignore = true)
  @Mapping(target = "recordedByUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  CabinetDecision toEntity(CreateCabinetDecisionRequestDto request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "paper", ignore = true)
  @Mapping(target = "recordedByUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void updateEntityFromDto(
      UpdateCabinetDecisionRequestDto request, @MappingTarget CabinetDecision decision);
}
