package com.teamtacles.teamtacles_api.service;

import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import com.teamtacles.teamtacles_api.dto.request.RoleRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.UserRequestDTO;
import com.teamtacles.teamtacles_api.dto.response.UserResponseDTO;
import com.teamtacles.teamtacles_api.exception.EmailAlreadyExistsException;
import com.teamtacles.teamtacles_api.exception.PasswordMismatchException;
import com.teamtacles.teamtacles_api.exception.ResourceNotFoundException;
import com.teamtacles.teamtacles_api.exception.UsernameAlreadyExistsException;
import com.teamtacles.teamtacles_api.mapper.PagedResponseMapper;
import com.teamtacles.teamtacles_api.model.Role;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.repository.RoleRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import java.util.stream.Collectors; 
import com.teamtacles.teamtacles_api.dto.response.RoleResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 
 * Service class responsible for managing user-related business logic in the TeamTacles application.
 * This includes operations such as creating new users, updating user roles, and retrieving user lists.
 * It interacts with repositories for data persistence and handles specific business exceptions.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-25
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PagedResponseMapper pagedResponseMapper;
    private final ModelMapper modelMapper; 


   public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, PagedResponseMapper pagedResponseMapper, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.pagedResponseMapper = pagedResponseMapper;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new user in the system based on the provided user request data.
     * Performs validation to ensure username/email uniqueness and password confirmation matching.
     * Newly created users are assigned the default ERole.USER role.
     *
     * @param userRequestDTO The UserRequestDTO containing the details for the new user.
     * @return A UserResponseDTO representing the newly created user.
     */
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) { 
        if(userRepository.existsByUserName(userRequestDTO.getUserName())){
            throw new UsernameAlreadyExistsException("Username/email already exists"); 
        }
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("Username/email already exists");
        }
        if(!userRequestDTO.getPassword().equals(userRequestDTO.getPasswordConfirm())){
            throw new PasswordMismatchException("Password and confirmation don't match");
        }

        User user = new User();
        user.setUserName(userRequestDTO.getUserName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        Role userRole = roleRepository.findByRoleName(ERole.USER)
            .orElseThrow(() -> new ResourceNotFoundException("Error: Role USER not found."));
        user.setRoles(Set.of(userRole));
        User savedUser = userRepository.save(user);

        UserResponseDTO userResponseDTO = modelMapper.map(savedUser, UserResponseDTO.class);

        // Mapeia roles para RoleResponseDTO
        if(savedUser.getRoles() != null) {
            userResponseDTO.setRoles(
                savedUser.getRoles().stream()
                    .map(roleEntity -> modelMapper.map(roleEntity, RoleResponseDTO.class))
                    .collect(Collectors.toSet())
            );
            
        }

        return userResponseDTO;
    }

    /**
     * Updates the role of an existing user.
     * This method retrieves a user by their ID, assigns a new role based on the request,
     * clearing any previous roles, and then saves the updated user.
     *
     * @param The unique ID of the user whose role is to be updated.
     * @param The RoleRequestDTO containing the new role name.
     * @return A UserResponseDTO representing the user with the updated role.
     */
    // patch Role
    public UserResponseDTO exchangepaperUser(Long id, RoleRequestDTO roleRequestDTO) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));  

        Role userNewRole = roleRepository.findByRoleName(ERole.valueOf(roleRequestDTO.getRole().toUpperCase()))
            .orElseThrow(() -> new ResourceNotFoundException("Error: Role not found."));

        //  limpa as roles atuais e adiciona a nova
        user.getRoles().clear(); 
        user.getRoles().add(userNewRole);

        User updatedUser = userRepository.save(user);
        UserResponseDTO userResponseDTO = modelMapper.map(updatedUser, UserResponseDTO.class);

        // Mapeia roles para RoleResponseDTO
        if(updatedUser.getRoles() != null) {
            userResponseDTO.setRoles(
                updatedUser.getRoles().stream()
                    .map(roleEntity -> modelMapper.map(roleEntity, RoleResponseDTO.class))
                    .collect(Collectors.toSet())
            );
        }

        
        return userResponseDTO;

    }

    /**
     * Retrieves a paginated list of all users registered in the system.
     *
     * @param pageable Pagination information (page number, page size, sorting).
     * @return A PagedResponse containing a page of UserResponseDTO objects.
     */
    public PagedResponse<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(users, UserResponseDTO.class);
    }
}
