package lk.gov.mohe.adminsystem.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "role", source = "role.name")
  @Mapping(target = "division", source = "division.name")
  UserDto toUserDto(User user);

  @Mapping(target = "email", ignore = true)
  @Mapping(target = "phoneNumber", ignore = true)
  @Mapping(target = "isActive", ignore = true)
  @Mapping(target = "role", source = "role.name")
  @Mapping(target = "division", source = "division.name")
  @Named("toUserDtoMin")
  UserDto toUserDtoMin(User user);
}
