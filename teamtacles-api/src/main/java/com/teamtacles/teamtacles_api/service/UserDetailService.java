package com.teamtacles.teamtacles_api.service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.UserAuthenticated;
import com.teamtacles.teamtacles_api.repository.UserRepository;

/**
 * Service implementation for Spring Security's UserDetailsService.
 * This class is responsible for loading user-specific data during the authentication process.
 * It fetches user details from the database and constructs a {UserAuthenticated object
 * that Spring Security can understand.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-24
 */

@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    private UserDetailService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username. 
     *
     * @param username The username identifying the user whose data is required.
     * @return A UserDetails object (specifically, a UserAuthenticated instance)
     * containing the user's authentication and authorization information.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       User user = userRepository.findByUserNameIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));

        return new UserAuthenticated(user);
    }
}
