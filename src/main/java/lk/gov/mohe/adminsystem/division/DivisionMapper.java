package lk.gov.mohe.adminsystem.division;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DivisionMapper {
    DivisionDto toDto(Division division);
}
