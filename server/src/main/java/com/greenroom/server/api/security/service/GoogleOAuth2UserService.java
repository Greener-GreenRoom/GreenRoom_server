package com.greenroom.server.api.security.service;


import com.greenroom.server.api.domain.user.repository.GradeRepository;
import com.greenroom.server.api.security.dto.GoogleOAuthAttribute;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final HttpSession httpSession;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        GoogleOAuthAttribute authAttribute = extractGoogleOAuth2UserAttributes(userRequest, delegate);
        User savedUser = save(authAttribute);

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

    @Transactional
    public User save(GoogleOAuthAttribute attribute){

        Optional<User> findUser = userRepository.findByEmail(attribute.getEmail());

        if(findUser.isPresent()){
            return userRepository.save(findUser.get().updateUser(attribute));
        }
        User user = User.createUser(attribute,gradeRepository.findById(1L).orElse(null));
        user.setDefaultPasswordOnOAuth2User(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }
}
