package com.greenroom.server.api.security.controller;


import com.greenroom.server.api.security.dto.LoginDto;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.handler.JWTFilter;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.security.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authManagerBuilder;
    private final CustomUserDetailService userDetailService;

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto.ResponseTokenDto> authorize(@RequestBody LoginDto loginDto, @AuthenticationPrincipal UserDetails user){

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        Authentication authentication = authManagerBuilder.getObject().authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenDto token = userDetailService.setTokens(authentication);

        return new ResponseEntity<>(
                TokenDto.ResponseTokenDto.builder()
                        .accessToken(token.getAccessToken())
                        .refreshToken(token.getRefreshToken())
                        .build(),
                JWTFilter.responseTokenWithHeaders(token.getAccessToken()),
                HttpStatus.OK
        );
    }
}
