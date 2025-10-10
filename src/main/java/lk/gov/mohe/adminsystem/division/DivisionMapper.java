package lk.gov.mohe.adminsystem.division;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DivisionMapper {
  DivisionDto toDto(Division division);
}
