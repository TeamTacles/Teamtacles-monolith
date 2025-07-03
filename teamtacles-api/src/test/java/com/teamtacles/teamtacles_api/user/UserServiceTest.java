package com.teamtacles.teamtacles_api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.teamtacles.teamtacles_api.dto.request.ProjectRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.RoleRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.UserRequestDTO;
import com.teamtacles.teamtacles_api.dto.response.RoleResponseDTO;
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
import com.teamtacles.teamtacles_api.service.UserService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @Mock
    private ModelMapper modelMapper; 

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create a user")
    void shouldCreateuserWithValidData(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUserName("Pedro Lauton");
        userRequestDTO.setEmail("lauton@gmail.com");
        userRequestDTO.setPassword("SenhaSegura123");
        userRequestDTO.setPasswordConfirm("SenhaSegura123");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleName(ERole.USER);

        User newUser = new User();
        newUser.setUserName(userRequestDTO.getUserName());
        newUser.setEmail(userRequestDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        newUser.setRoles(Set.of(userRole));

        User savedNewUser = new User();
        savedNewUser.setUserName(newUser.getUserName());
        savedNewUser.setEmail(newUser.getEmail());
        savedNewUser.setPassword(newUser.getPassword());
        savedNewUser.setRoles(newUser.getRoles());

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setEmail(savedNewUser.getEmail());
        userResponseDTO.setUserName(savedNewUser.getUserName());
        userResponseDTO.setRoles(Set.of(new RoleResponseDTO(ERole.USER)));

        Role userRoleReturned = new Role();
        userRoleReturned.setId(1L);
        userRoleReturned.setRoleName(ERole.USER);

        when(userRepository.existsByUserName(userRequestDTO.getUserName())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(ERole.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(newUser)).thenReturn(savedNewUser);
        when(modelMapper.map(savedNewUser, UserResponseDTO.class)).thenReturn(userResponseDTO);
        when(modelMapper.map(userRoleReturned, RoleResponseDTO.class)).thenReturn(new RoleResponseDTO(userRoleReturned.getRoleName()));

        UserResponseDTO response = userService.createUser(userRequestDTO);

        assertNotNull(response);
        assertEquals(userRequestDTO.getEmail(), response.getEmail());
        assertEquals(userRequestDTO.getUserName(), response.getUserName());
        assertTrue(response.getRoles().stream().anyMatch(role -> role.getRoleName().equals(userRoleReturned.getRoleName())));
    }

    @Test
    @DisplayName("Should throw UsernameAlreadyExistsException when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUserName("Pedro Lauton");
        userRequestDTO.setEmail("lauton@gmail.com");
        userRequestDTO.setPassword("SenhaSegura123");
        userRequestDTO.setPasswordConfirm("SenhaSegura123");

        when(userRepository.existsByUserName(userRequestDTO.getUserName())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(userRequestDTO));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when username already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUserName("Pedro Lauton");
        userRequestDTO.setEmail("lauton@gmail.com");
        userRequestDTO.setPassword("SenhaSegura123");
        userRequestDTO.setPasswordConfirm("SenhaSegura123");

        when(userRepository.existsByUserName(userRequestDTO.getUserName())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userRequestDTO));
    }

    @Test
    @DisplayName("Should throw PasswordMismatchException when username already exists")
    void shouldThrowExceptionWhenPasswordMismatch(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUserName("Pedro Lauton");
        userRequestDTO.setEmail("lauton@gmail.com");
        userRequestDTO.setPassword("SenhaSegura");
        userRequestDTO.setPasswordConfirm("SenhaSegura123");

        when(userRepository.existsByUserName(userRequestDTO.getUserName())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () -> userService.createUser(userRequestDTO));
    }

    @Test
    @DisplayName("Should update the user's role successfully")
    void shouldUpdateUserRoleSuccessfully(){
        Long userId = 1L;

        RoleRequestDTO roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setRole("ADMIN");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleName(ERole.USER);

        User existingUser = new User();
        existingUser.setUserName("Pedro Ramos");
        existingUser.setEmail("ramos@gmail.com");
        existingUser.setPassword(passwordEncoder.encode("SenhaSegura123"));
        existingUser.setRoles(new HashSet<>(Set.of(userRole)));

        Role userRoleReturned = new Role();
        userRoleReturned.setId(2L);
        userRoleReturned.setRoleName(ERole.ADMIN);
        existingUser.setRoles(new HashSet<>(Set.of(userRoleReturned)));

        User savedNewUser = new User();
        savedNewUser.setUserName(existingUser.getUserName());
        savedNewUser.setEmail(existingUser.getEmail());
        savedNewUser.setPassword(existingUser.getPassword());
        savedNewUser.setRoles(new HashSet<>(Set.of(userRoleReturned)));

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setEmail(savedNewUser.getEmail());
        userResponseDTO.setUserName(savedNewUser.getUserName());
        userResponseDTO.setRoles(Set.of(new RoleResponseDTO(userRoleReturned.getRoleName())));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByRoleName(ERole.valueOf(roleRequestDTO.getRole().toUpperCase()))).thenReturn(Optional.of(userRoleReturned));
        when(userRepository.save(existingUser)).thenReturn(savedNewUser);
        when(modelMapper.map(savedNewUser, UserResponseDTO.class)).thenReturn(userResponseDTO);
        when(modelMapper.map(userRoleReturned, RoleResponseDTO.class)).thenReturn(new RoleResponseDTO(userRoleReturned.getRoleName()));

        UserResponseDTO response = userService.exchangepaperUser(userId, roleRequestDTO);

        assertNotNull(response);
        assertTrue(response.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.valueOf(roleRequestDTO.getRole()))));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFound(){
        Long userId = 56L;

        RoleRequestDTO roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setRole("ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class,() -> userService.exchangepaperUser(userId, roleRequestDTO));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user role does not exist")
    void shouldThrowExceptionWhenUserRoleNotExist(){
        Long userId = 1L;

        RoleRequestDTO roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setRole("INVALID_ROLE");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleName(ERole.USER);

        User existingUser = new User();
        existingUser.setUserName("Pedro Ramos");
        existingUser.setEmail("ramos@gmail.com");
        existingUser.setPassword(passwordEncoder.encode("SenhaSegura123"));
        existingUser.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class,() -> userService.exchangepaperUser(userId, roleRequestDTO));
    }
}