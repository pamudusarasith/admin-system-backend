package lk.gov.mohe.adminsystem.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "division", source = "division.name")
    UserDto toUserDto(User user);
}
