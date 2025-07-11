package org.example.intershop.mapper;

import org.example.intershop.domain.User;
import org.example.intershop.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User userDtoToUser(UserDto userDto);
    UserDto userToUserDto(User user);
}
