package com.teamtacles.teamtacles_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.teamtacles.teamtacles_api.security.CustomJwtAuthenticationConverter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}") 
    private RSAPublicKey key;
    @Value("${jwt.private.key}")
    private RSAPrivateKey priv; 

    @Bean
    public CustomJwtAuthenticationConverter customJwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    } 

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
        CustomJwtAuthenticationConverter customJwtAuthenticationConverter) throws AuthenticationCredentialsNotFoundException, AccessDeniedException, Exception {

        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions().sameOrigin()) //
                .authorizeHttpRequests(auth ->  auth.requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(
                        conf -> conf.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();  
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        var jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk)); 
        return new NimbusJwtEncoder(jwks);
    }
}