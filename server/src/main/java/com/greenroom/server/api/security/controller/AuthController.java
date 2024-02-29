package com.greenroom.server.api.security.controller;


import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.security.dto.LoginDto;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.handler.JWTFilter;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManagerBuilder authManagerBuilder;
    private final CustomUserDetailService userDetailService;

    @GetMapping("/authenticate/{userEmail}")
    public ResponseEntity<ApiResponse> authorizeWithOAuth(@PathVariable String userEmail){

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEmail,"password");
        Authentication authentication = authManagerBuilder.getObject().authenticate(authToken);
        TokenDto token = userDetailService.setTokens(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new ResponseEntity<>(
                ApiResponse.success(
                        TokenDto.ResponseTokenDto.builder()
                                .accessToken(token.getAccessToken())
                                .refreshToken(token.getRefreshToken())
                                .build()
                ),
                JWTFilter.responseTokenWithHeaders(token.getAccessToken()),
                HttpStatus.OK
        );
    }

}
