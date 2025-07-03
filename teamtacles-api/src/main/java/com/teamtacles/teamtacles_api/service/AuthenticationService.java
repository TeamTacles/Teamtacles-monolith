package com.teamtacles.teamtacles_api.service;

import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.exception.ResourceNotFoundException;
import com.teamtacles.teamtacles_api.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling the authentication process and
 * generating JWT tokens for authenticated users in the TeamTacles application.
 * It integrates with Spring Security's authentication mechanism to provide
 * a JWT upon successful authentication.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-25
 */
@Service
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    public AuthenticationService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user based on the provided Spring Security Authentication object
     * and generates a JWT token for the authenticated user.
     *
     * @param authentication The Authentication object containing the authenticated user's details.
     * @return A String representing the generated JWT token.
     */
    public String authenticate(Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUserNameIgnoreCase(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not Found."));
        return jwtService.generateToken(user);
    }
}
