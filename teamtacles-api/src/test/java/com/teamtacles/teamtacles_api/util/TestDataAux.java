package com.teamtacles.teamtacles_api.util;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.teamtacles.teamtacles_api.model.Role;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.repository.RoleRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.service.JwtService;

@Component
public class TestDataAux {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User normalUser;
    private String userToken;

    private User adminUser;
    private String adminToken;

    public void setUpTestUsers(){

        // search for user role or create an user role and returns it
        Role userRoleEntity = roleRepository.findByRoleName(ERole.USER)
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setRoleName(ERole.USER);
                return roleRepository.save(newRole);
            });

        // search for admin role or create an admin role and returns it
        Role adminRoleEntity = roleRepository.findByRoleName(ERole.ADMIN)
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setRoleName(ERole.ADMIN);
                return roleRepository.save(newRole);
            });

        // create a test user
        normalUser = new User();
        normalUser.setUserName("testuser");
        normalUser.setPassword(passwordEncoder.encode("12345"));
        normalUser.setEmail("testuser@example.com");
        normalUser.setRoles(Set.of(userRoleEntity));
        userRepository.save(normalUser);
        userToken = jwtService.generateToken(normalUser);

        // create a test admin
        adminUser = new User();
        adminUser.setUserName("testadmin");
        adminUser.setPassword(passwordEncoder.encode("12345"));
        adminUser.setEmail("testadmin@example.com");
        adminUser.setRoles(Set.of(adminRoleEntity));
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);
    }

    public User getNormalUser(){
        if(normalUser == null){
            throw new IllegalStateException("Tests user not set up yet. Call setUpTestUsers() first");
        }
        return normalUser;
    }

    public String getNormalUserToken(){
        if(userToken == null){
            throw new IllegalStateException("Tests user not set up yet. Call setUpTestUsers() first");
        }
        return userToken;
    }

        public User getAdminUser(){
        if(adminUser == null){
            throw new IllegalStateException("Tests user not set up yet. Call setUpTestUsers() first");
        }
        return adminUser;
    }

    public String getAdminUserToken(){
        if(adminToken == null){
            throw new IllegalStateException("Tests user not set up yet. Call setUpTestUsers() first");
        }
        return adminToken;
    }

}
