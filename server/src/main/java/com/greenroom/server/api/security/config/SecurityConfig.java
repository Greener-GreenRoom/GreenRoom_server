package com.greenroom.server.api.security.config;

import com.greenroom.server.api.security.service.GoogleOAuth2UserService;
import com.greenroom.server.api.security.handler.JWTAccessDeniedHandler;
import com.greenroom.server.api.security.handler.JWTAuthenticationEntryPoint;
import com.greenroom.server.api.security.handler.JWTFilter;
import com.greenroom.server.api.security.handler.OAuth2AuthenticationSuccessHandler;
import com.greenroom.server.api.security.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JWTAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final TokenProvider tokenProvider;
    private static final String[] ANONYMOUS_MATCHERS = {
            "/", "/login/**", "/api/user/signup","/api/authenticate/**","/login/oauth2/code/google/**","/error",

            // 테스트용 메서드
            "/api/user/delete"
    };
//    private static final String[] STATIC_RESOURCES = {
////            "/h2-console/**"
//            "/favicon.ico", "/css/**","/js/**","/image/**"
//    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .headers((headerConfig) ->
                        headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .authorizeHttpRequests((authorizeRequests) ->
                                authorizeRequests
                                        .requestMatchers(
                                                Stream.of(ANONYMOUS_MATCHERS)
                                                        .map(uri->new MvcRequestMatcher(introspector,uri))
                                                        .toArray(MvcRequestMatcher[]::new)
                                        ).permitAll()
//                                        .requestMatchers(
//                                                Stream.of(STATIC_RESOURCES)
//                                                        .map(uri->new MvcRequestMatcher(introspector,uri))
//                                                        .toArray(MvcRequestMatcher[]::new)
//                                        ).permitAll()
                                        .anyRequest().authenticated()
                )
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .sessionManagement((sessionManagement)->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JWTFilter(tokenProvider),UsernamePasswordAuthenticationFilter.class)
                .oauth2Login((oauthConfig) ->
                        oauthConfig.userInfoEndpoint((userInfoEndpointConfig) ->
                                        userInfoEndpointConfig.userService(googleOAuth2UserService)
                                )
                                .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .build();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
//                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                .requestMatchers(new AntPathRequestMatcher( "/favicon.ico"))
                .requestMatchers(new AntPathRequestMatcher( "/css/**"))
                .requestMatchers(new AntPathRequestMatcher( "/js/**"))
                .requestMatchers(new AntPathRequestMatcher( "/image/**"));
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
