package com.greenroom.server.api.security.service;

import com.greenroom.server.api.domain.greenroom.repository.GradeRepository;
import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.enums.Provider;
import com.greenroom.server.api.domain.user.enums.Role;
import com.greenroom.server.api.domain.user.enums.UserStatus;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.security.dto.AuthorizeDto;
import com.greenroom.server.api.security.dto.TokenDto;
import com.greenroom.server.api.security.exception.AllTokenExpiredException;
import com.greenroom.server.api.security.exception.NotFoundTokens;
import com.greenroom.server.api.security.exception.OtherOAuth2Exception;
import com.greenroom.server.api.security.util.TokenProvider;
import com.greenroom.server.api.utils.ImageUploader.UserProfileImageUploader;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileImageUploader userProfileImageUploader;

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

    public User getUser(final String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
    }

    /**
     * token 상태 업데이트
     */
    @Transactional
    public TokenDto setTokens(Authentication authentication, AuthorizeDto dto){

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
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

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));

        TokenDto token = tokenProvider.createAllToken(authentication);

        user = user.invalidateAllTokens()
                .setRefreshToken(token.getRefreshToken(), tokenProvider.extractExpiration(token.getRefreshToken()))
                .setAccessToken(token.getAccessToken(), tokenProvider.extractExpiration(token.getAccessToken()));

        userRepository.save(user);
        return token;
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
        // TODO : 2개 이상의 oauth 인 경우 이메일 도메인 앞 닉네임으로 중복 판별할 것. -> google 계정 이메일로 이미 가입했다면 kakao 가입 불가
        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());

        if(findUser.isPresent()){
            return userRepository.save(findUser.get().updateUserName(userDto.getName()));
        }

        User user = User
                .createUser(userDto,gradeRepository.findById(1L).orElse(null))
                .setDefaultPasswordOnOAuth2User(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userEmail,String withdrawalReason){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));
        user.withdrawalUser(withdrawalReason);
        userRepository.save(user);
    }

    @Transactional
    public int deleteAllUserInDeletePending(){
        return userRepository.deleteAllByStatus(UserStatus.DELETE_PENDING);
    }

    @Transactional
    public User updateUser(UserDto.UpdateUserRequest userDto,String userEmail,MultipartFile imageFile) throws IOException {
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재 하지 않습니다."));

        String profileUrl = user.getProfileUrl();

        if(!imageFile.isEmpty()){
            profileUrl = userProfileImageUploader.uploadUserProfileImage(imageFile);
        }

        return userRepository.save(
                user
                        .updateProfileUrl(profileUrl)
                        .updateUserName(userDto.getName())
        );
    }


    @Transactional
    public Integer getUserLevel(String userEmail){
        return userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음.")).getGrade().getLevel();
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
