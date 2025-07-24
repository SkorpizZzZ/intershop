package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.domain.Cart;
import org.example.intershop.domain.User;
import org.example.intershop.dto.UserDto;
import org.example.intershop.enums.Role;
import org.example.intershop.mapper.UserMapper;
import org.example.intershop.repository.CartRepository;
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
    private final CartRepository cartRepository;
    private final UserMapper userMapper;

    @Transactional
    public Mono<UserDto> create(String username, String encodedPassword, Role role) {
        return Mono.zip(saveUser(username, encodedPassword, role), saveCart(username))
                .map(tuple -> userMapper.userToUserDto(tuple.getT1()));
    }

    private Mono<Cart> saveCart(String username) {
        return cartRepository.save(Cart.builder()
                .username(username)
                .build()
        );
    }

    private Mono<User> saveUser(String username, String encodedPassword, Role role) {
        return userRepository.save(
                User.builder()
                        .username(username)
                        .password(encodedPassword)
                        .role(role)
                        .build()
        );
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }
}
