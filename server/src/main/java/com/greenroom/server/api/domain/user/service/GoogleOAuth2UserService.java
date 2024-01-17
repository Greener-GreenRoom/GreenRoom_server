package com.greenroom.server.api.domain.user.service;


import com.greenroom.server.api.domain.user.dto.GoogleOAuthAttribute;
import com.greenroom.server.api.domain.user.dto.SessionUser;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        GoogleOAuthAttribute authAttribute = extractGoogleOAuth2UserAttributes(userRequest, delegate);
        User savedUser = save(authAttribute);
        httpSession.setAttribute("user",new SessionUser(savedUser));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(
                        savedUser.getRole().toString())),
                authAttribute.getAttributes(),
                authAttribute.getNameAttributeKey()
        );
    }

    private static GoogleOAuthAttribute extractGoogleOAuth2UserAttributes(OAuth2UserRequest userRequest, OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        return GoogleOAuthAttribute.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
    }

    private User save(GoogleOAuthAttribute attribute){
        return userRepository.save(
                userRepository
                        .findByEmail(attribute.getEmail())
                        .orElse(attribute.toEntity())
        );
    }
}
