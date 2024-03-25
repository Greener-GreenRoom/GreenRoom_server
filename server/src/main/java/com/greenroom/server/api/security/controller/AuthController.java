package com.greenroom.server.api.security.controller;


import com.greenroom.server.api.security.dto.AuthorizeDto;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.dto.TokenRequestDto;
import com.greenroom.server.api.security.handler.JWTFilter;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManagerBuilder authManagerBuilder;
    private final CustomUserDetailService userDetailService;

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse> authorize(@RequestBody AuthorizeDto dto){

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.getUserEmail(),"password");
        Authentication authentication = authManagerBuilder.getObject().authenticate(authToken);

        TokenDto token = userDetailService.setTokens(authentication,dto);
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

    @PostMapping("/authenticate/issue")
    public ResponseEntity<ApiResponse> issueAllTokens(@RequestBody TokenRequestDto dto){

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.getEmail(),"password");
        Authentication authentication = authManagerBuilder.getObject().authenticate(authToken);

        TokenDto token = userDetailService.issueAllTokens(authentication);
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

    @DeleteMapping("/authenticate/logout")
    public ResponseEntity<ApiResponse> logout(@AuthenticationPrincipal User user){
        userDetailService.remoteAllTokens(user);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
