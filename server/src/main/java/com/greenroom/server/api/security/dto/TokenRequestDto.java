package com.greenroom.server.api.security.dto;

import com.greenroom.server.api.domain.user.enums.Provider;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequestDto {
    private String email;
}
