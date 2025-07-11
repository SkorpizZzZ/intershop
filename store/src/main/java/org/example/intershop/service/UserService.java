package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.domain.User;
import org.example.intershop.dto.UserDto;
import org.example.intershop.enums.Role;
import org.example.intershop.mapper.UserMapper;
import org.example.intershop.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public Mono<UserDto> create(String username, String encodedPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        return userRepository.save(user)
                .map(userMapper::userToUserDto);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }
}
