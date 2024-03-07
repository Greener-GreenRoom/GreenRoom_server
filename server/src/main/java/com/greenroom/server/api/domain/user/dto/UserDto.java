package com.greenroom.server.api.domain.user.dto;

import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private String name;
    private String email;
    private String profileUrl;
    private Provider provider;

    public static UserDto toDto(User user){
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .profileUrl(user.getProfileUrl())
                .provider(user.getProvider())
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateUserRequest{
        private String name;
    }
}
