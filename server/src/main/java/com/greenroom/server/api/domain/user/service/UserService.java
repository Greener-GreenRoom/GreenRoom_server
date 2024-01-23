package com.greenroom.server.api.domain.user.service;

import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Role;
import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(UserDto userDto){

        if(userRepository.findByEmail(userDto.getEmail()).isPresent()){
            throw new UserAlreadyExist(ResponseCodeEnum.ALREADY_EXIST,"이미 존재하는 유저 입니다.");
        }

        return userRepository.save(
                User.builder()
                        .name(userDto.getName())
                        .password(passwordEncoder.encode(userDto.getPassword()))
                        .email(userDto.getEmail())
                        .role(Role.GENERAL)
                        .build()
        );
    }
}
