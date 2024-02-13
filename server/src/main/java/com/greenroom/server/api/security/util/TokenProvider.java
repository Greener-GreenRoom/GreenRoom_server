package com.greenroom.server.api.security.util;

import com.greenroom.server.api.security.dto.TokenDto;
import com.nimbusds.jwt.JWT;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider implements InitializingBean {

    private final String SECRET_KEY;
    private static final String AUTHORITIES_KEY = "auth";
    private final long accessTokenValidityInMilliSeconds; // access token : 24h
    private final long refreshTokenValidityInMilliSeconds; // refresh token : 7d
    private Key key;

    public TokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds){
        this.SECRET_KEY = secretKey;
        this.accessTokenValidityInMilliSeconds = tokenValidityInSeconds*1000;
        this.refreshTokenValidityInMilliSeconds = accessTokenValidityInMilliSeconds*7;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    public LocalDateTime extractExpiration(String token){
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiration.getTime()), ZoneId.of("Asia/Seoul"));
    }

    public boolean isExpired(String token){
        try{
            return extractExpiration(token).isBefore(LocalDateTime.now());}
        catch (ExpiredJwtException e) {
            return true;}
    }

    public TokenDto createAllToken(Authentication authentication) { // 토큰 생성

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return TokenDto.builder()
                .accessToken(
                        Jwts.builder()
                                .setSubject(authentication.getName())
                                .claim(AUTHORITIES_KEY, authorities)
                                .signWith(key, SignatureAlgorithm.HS512)
                                .setExpiration(createTokenValidity(this.accessTokenValidityInMilliSeconds))
                                .compact()
                )
                .refreshToken(
                        Jwts.builder()
                                .setSubject(authentication.getName())
                                .signWith(key, SignatureAlgorithm.HS512)
                                .setExpiration(createTokenValidity(this.refreshTokenValidityInMilliSeconds))
                                .compact()
                )
                .email(authentication.getName())
                .build();
    }

    public TokenDto updateAccessToken(Authentication authentication,String refreshToken) {
        //access token 갱신
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return TokenDto.builder()
                .accessToken(
                        Jwts.builder()
                                .setSubject(authentication.getName())
                                .claim(AUTHORITIES_KEY, authorities)
                                .signWith(key, SignatureAlgorithm.HS512)
                                .setExpiration(createTokenValidity(this.accessTokenValidityInMilliSeconds))
                                .compact()
                )
                .refreshToken(refreshToken)
                .email(authentication.getName())
                .build();
    }

    public TokenDto updateRefreshToken(Authentication authentication,String accessToken) {
        //access token 갱신
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(
                        Jwts.builder()
                        .setSubject(authentication.getName())
                        .signWith(key, SignatureAlgorithm.HS512)
                        .setExpiration(createTokenValidity(this.refreshTokenValidityInMilliSeconds))
                        .compact()
                )
                .email(authentication.getName())
                .build();
    }
    private Date createTokenValidity(long milliseconds){
        return new Date((new Date()).getTime() + milliseconds);
    }
    public boolean isUpdatableRefreshToken(String token){
        return LocalDateTime.now().plusDays(7).isBefore(extractExpiration(token));
    }
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("invalidate jwt signature");
        } catch (ExpiredJwtException e) {
            log.info("jwt token was expired");
        } catch (UnsupportedJwtException e) {
            log.info("unsupported jwt token");
        } catch (IllegalArgumentException e) {
            log.info("invalid jwt token");
        }
        return false;
    }
}
