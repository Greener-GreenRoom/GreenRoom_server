package com.greenroom.server.api.security.dto;

import com.greenroom.server.api.domain.user.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeDto {

    private String userEmail;
    private Provider provider;
    private String accessToken;
    private String refreshToken;
}
