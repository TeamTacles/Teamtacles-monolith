package com.teamtacles.teamtacles_api.model;

import jakarta.persistence.Entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implements the UserDetails interface for Spring Security,
 * providing a representation of an authenticated user within the TeamTacles application.
 * This class wraps the core User entity and exposes its necessary
 * authentication and authorization details for Spring Security's framework.
 *
 * It acts as an adapter, translating the application's User model
 * into a format consumable by Spring Security.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-23
 */
public class UserAuthenticated implements UserDetails {
    
    private final User user;

    public UserAuthenticated(User user) {
        this.user = user;
    } 

    public User getUser() {
        return user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getRoleName().name())
            .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}