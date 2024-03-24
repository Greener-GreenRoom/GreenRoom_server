package com.greenroom.server.api.security.service;

import com.greenroom.server.api.domain.greenroom.repository.GradeRepository;
import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Role;
import com.greenroom.server.api.domain.user.enums.UserStatus;
import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.security.dto.AuthorizeDto;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.exception.AllTokenExpiredException;
import com.greenroom.server.api.security.exception.NotFoundTokens;
import com.greenroom.server.api.security.exception.OtherOAuth2Exception;
import com.greenroom.server.api.security.util.TokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 순수 user 관련 생명 주기만 관리 하는 서비스
 * (인증, 인가, 생성, 삭제)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Map<Role,List<GrantedAuthority>> authorityMap = new HashMap<>();

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        return getUserDetails(findUserByEmail(email));
    }

    private UserDetails getUserDetails(User user){
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),authorityMap.get(user.getRole()));
    }

    /**
     * token 상태 업데이트
     */
    public User findUserByEmail(final String userEmail){
        return userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
    }

    @Transactional
    public TokenDto setTokens(Authentication authentication, AuthorizeDto dto){

        User user = findUserByEmail(authentication.getName());
        TokenDto token = null;

        if (dto.getProvider() != user.getProvider()) {
            throw new OtherOAuth2Exception(ResponseCodeEnum.ALREADY_EXIST_OTHER_OAUTH);
        }else if(!StringUtils.hasText(dto.getAccessToken()) || !StringUtils.hasText(dto.getRefreshToken())){
            throw new NotFoundTokens(ResponseCodeEnum.TOKENS_NOT_FOUND);
        }else if(tokenProvider.isExpired(dto.getAccessToken()) &&
                tokenProvider.isExpired(dto.getRefreshToken()) &&
                user.getAccessToken().equals(dto.getAccessToken()) &&
                user.getRefreshToken().equals(dto.getRefreshToken())
        ){
            throw new AllTokenExpiredException(ResponseCodeEnum.ALL_TOKEN_WERE_EXPIRED);
        }

        if(isUpdatableAccessTokenCond(dto.getRefreshToken(),dto.getAccessToken(),user)){
            token = tokenProvider.updateAccessToken(authentication,user.getRefreshToken());
            user.setAccessToken(token.getAccessToken(),tokenProvider.extractExpiration(token.getAccessToken()));
        }

        if(isUpdatableRefreshTokenCond(dto.getRefreshToken(),user)){
            //refresh token 이 만료가 7일 이내 일 경우 refresh token 을 갱신
            token = tokenProvider.updateRefreshToken(authentication,user.getAccessToken());
            user.setRefreshToken(token.getRefreshToken(),tokenProvider.extractExpiration(token.getRefreshToken()));
        }
        userRepository.save(user);
        return token;
    }

    /**
     * 모든 토큰 발급
     */
    @Transactional
    public TokenDto issueAllTokens(Authentication authentication){

        User user = findUserByEmail(authentication.getName());

        TokenDto token = tokenProvider.createAllToken(authentication);

        user = user.invalidateAllTokens()
                .setRefreshToken(token.getRefreshToken(), tokenProvider.extractExpiration(token.getRefreshToken()))
                .setAccessToken(token.getAccessToken(), tokenProvider.extractExpiration(token.getAccessToken()));

        userRepository.save(user);
        return token;
    }

    @Transactional
    public void remoteAllTokens(UserDetails userDetails){

        User user = findUserByEmail(userDetails.getUsername())
                .invalidateAllTokens();
        userRepository.save(user);
    }

    private boolean isUpdatableAccessTokenCond(String refreshToken,String accessToken,User user) {
        return !tokenProvider.isExpired(refreshToken) &&
                user.getRefreshToken().equals(refreshToken) &&
                user.getRefreshTokenExpirationTime().isAfter(tokenProvider.extractExpiration(accessToken)) &&
                user.getAccessToken().equals(accessToken);
    }

    private boolean isUpdatableRefreshTokenCond(String refreshToken,User user) {
        return !tokenProvider.isExpired(refreshToken) &&
                tokenProvider.checkUpdatableRefreshToken(refreshToken,user.getRefreshTokenExpirationTime()) &&
                user.getRefreshToken().equals(refreshToken);
    }


    @Transactional
    public User save(UserDto userDto){

        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());

        if(findUser.isPresent()){

            User existUser = findUser.get();

            if(!existUser.getProvider().equals(userDto.getProvider())){
                throw new OtherOAuth2Exception(ResponseCodeEnum.ALREADY_EXIST_OTHER_OAUTH);
            }else{
                throw new UserAlreadyExist(ResponseCodeEnum.ALREADY_EXIST);
            }
        }

        User user = User
                .createUser(userDto,gradeRepository.findByLevel(0).orElse(null))
                .setDefaultPasswordOnOAuth2User(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userEmail){
        User user = findUserByEmail(userEmail);
        user.withdrawalUser();
        userRepository.save(user);
    }

    @Transactional
    public int deleteAllUserInDeletePending(){
        return userRepository.deleteAllByStatus(UserStatus.DELETE_PENDING);
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
