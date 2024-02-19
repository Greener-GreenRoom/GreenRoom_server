package com.greenroom.server.api.security.service;

import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Role;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.util.TokenProvider;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    private static final Map<Role,List<GrantedAuthority>> authorityMap = new HashMap<>();

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
        return getUserDetails(user);
    }

    private UserDetails getUserDetails(User user){
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),authorityMap.get(user.getRole()));
    }
    @Transactional
    public TokenDto setTokens(Authentication authentication){

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
        TokenDto token = null;

        ///에러 방지 임시책
        if(!isFirstAuthentication(user)){
            token = new TokenDto(user.getEmail(),user.getAccessToken(), user.getRefreshToken());
        }

        if(isFirstAuthentication(user)){
            // 처음으로 인증할 떄 access refresh 둘 다 발급
            token = tokenProvider.createAllToken(authentication);
            user.setRefreshToken(token.getRefreshToken(),tokenProvider.extractExpiration(token.getRefreshToken()));
            user.setAccessToken(token.getAccessToken(),tokenProvider.extractExpiration(token.getAccessToken()));

        }else if(tokenProvider.isExpired(token.getAccessToken()) &&
                !tokenProvider.isExpired(token.getRefreshToken()) &&
                user.getRefreshToken().equals(token.getRefreshToken())) {
            //refresh token 만료시간이 지나지 않았지만 access token 이 만료 되었을 때
            token = tokenProvider.updateAccessToken(authentication,user.getRefreshToken());
            user.setAccessToken(token.getAccessToken(),tokenProvider.extractExpiration(token.getAccessToken()));

        }else if(tokenProvider.isUpdatableRefreshToken(token.getRefreshToken()) &&
                user.getRefreshToken().equals(token.getRefreshToken())){
            //refresh token 이 만료가 7일 이내 일 경우 refresh token 을 갱신
            token = tokenProvider.updateRefreshToken(authentication,user.getAccessToken());
            user.setRefreshToken(token.getRefreshToken(),tokenProvider.extractExpiration(token.getRefreshToken()));
        }

        else if(tokenProvider.isExpired(token.getAccessToken()) &&
                tokenProvider.isExpired(token.getRefreshToken())){
            // access token과 refresh 토큰이 모두 만료 되어 다시 로그인 했을 경우
            token = tokenProvider.createAllToken(authentication);
            user.setRefreshToken(token.getRefreshToken(),tokenProvider.extractExpiration(token.getRefreshToken()));
            user.setAccessToken(token.getAccessToken(),tokenProvider.extractExpiration(token.getAccessToken()));
        }

        return token;
    }


    private static boolean isFirstAuthentication(User user) {
        return !StringUtils.hasText(user.getRefreshToken());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setAuthoritiesMap(){
        List<GrantedAuthority> guestAuthorities = new ArrayList<GrantedAuthority>();
        guestAuthorities.add(new SimpleGrantedAuthority("guest"));

        List<GrantedAuthority> generalAuthorities = new ArrayList<GrantedAuthority>();
        generalAuthorities.add(new SimpleGrantedAuthority("guest"));
        generalAuthorities.add(new SimpleGrantedAuthority("general"));

        List<GrantedAuthority> adminAuthorities = new ArrayList<GrantedAuthority>();
        adminAuthorities.add(new SimpleGrantedAuthority("guest"));
        adminAuthorities.add(new SimpleGrantedAuthority("general"));
        adminAuthorities.add(new SimpleGrantedAuthority("admin"));

        authorityMap.put(Role.GUEST,guestAuthorities);
        authorityMap.put(Role.GENERAL,generalAuthorities);
        authorityMap.put(Role.ADMIN,adminAuthorities);
    }
}
