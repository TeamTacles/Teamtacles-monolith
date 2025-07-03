package com.teamtacles.teamtacles_api.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;

import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.UserAuthenticated;



public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt){
        UserAuthenticated userAuthenticated = extractUser(jwt);
        List<GrantedAuthority> authorities = List.copyOf(userAuthenticated.getAuthorities());

        return new UsernamePasswordAuthenticationToken(userAuthenticated, null, authorities);
    }

    private UserAuthenticated extractUser(Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return new UserAuthenticated(user);
    }

}
