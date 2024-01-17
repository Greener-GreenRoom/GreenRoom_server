package com.greenroom.server.api.config.security;

import com.greenroom.server.api.domain.user.service.GoogleOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GoogleOAuth2UserService googleOAuth2UserService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .headers((headerConfig) ->
                        headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .authorizeHttpRequests((authorizeRequests) ->
                                authorizeRequests
                                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                                        .requestMatchers("/", "/css/**", "/images/**", "/js/**").permitAll()
                                        .requestMatchers("/", "/login/**").permitAll()
//                                .requestMatchers("/api/**").hasRole(Role.USER.name())
                                        .anyRequest().authenticated()
                )
                .logout((logoutConfig) ->
                        logoutConfig.logoutSuccessUrl("/")
                )
                .oauth2Login((oauthConfig) ->
                        oauthConfig.userInfoEndpoint((userInfoEndpointConfig) ->
                                userInfoEndpointConfig.userService(googleOAuth2UserService)
                        )
                ).build();
    }
}
