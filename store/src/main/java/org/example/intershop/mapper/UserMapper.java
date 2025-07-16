package org.example.intershop.mapper;

import org.example.intershop.domain.User;
import org.example.intershop.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    User userDtoToUser(UserDto userDto);
    UserDto userToUserDto(User user);
}
