package com.greenroom.server.api.security.config;

import com.greenroom.server.api.security.handler.JWTAccessDeniedHandler;
import com.greenroom.server.api.security.handler.JWTAuthenticationEntryPoint;
import com.greenroom.server.api.security.handler.JWTFilter;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JWTAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenProvider tokenProvider;
    private final JWTFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .headers((headerConfig) ->
                        headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests((authorizeRequests) ->
                                authorizeRequests
//                                        .requestMatchers(PathRequest.toH2Console()).permitAll()
//                                        .requestMatchers("/", "/css/**", "/images/**", "/js/**").permitAll()
                                        .requestMatchers(new MvcRequestMatcher(introspector,"/")).permitAll()
                                        .requestMatchers(new MvcRequestMatcher(introspector,"/login/**")).permitAll()
                                        .requestMatchers(new MvcRequestMatcher(introspector,"/api/user/signup")).permitAll()
                                        .requestMatchers(new MvcRequestMatcher(introspector,"/api/authenticate")).permitAll()
                                        .requestMatchers(new MvcRequestMatcher(introspector,"/gardening-data")).permitAll() /// plant data 주입 api 추가
//                                .requestMatchers("/api/**").hasRole(Role.USER.name())
                                        .anyRequest().authenticated()
                )
                .sessionManagement((sessionManagement)->
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class)
                .logout((logoutConfig) ->
                        logoutConfig.logoutSuccessUrl("/")
                )
//                .oauth2Login((oauthConfig) ->
//                        oauthConfig.userInfoEndpoint((userInfoEndpointConfig) ->
//                                userInfoEndpointConfig.userService(googleOAuth2UserService)
//                        )
//                )
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                .requestMatchers(new AntPathRequestMatcher( "/favicon.ico"))
                .requestMatchers(new AntPathRequestMatcher( "/css/**"))
                .requestMatchers(new AntPathRequestMatcher( "/js/**"))
                .requestMatchers(new AntPathRequestMatcher( "/image/**"));
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
