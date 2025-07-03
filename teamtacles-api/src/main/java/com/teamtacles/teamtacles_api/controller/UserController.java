package com.teamtacles.teamtacles_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import com.teamtacles.teamtacles_api.dto.request.RoleRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.UserRequestDTO;
import com.teamtacles.teamtacles_api.dto.response.UserResponseDTO;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for managing user-related operations in the TeamTacles application.
 * This controller provides endpoints for user registration, changing user roles, and retrieving lists of users.
 * It leverages Spring Security for access control, notably for administrative functions.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-25
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService){
        this.userService = userService;
    }

     /**
     * Registers a new user with the provided details.
     * This endpoint allows new users to create an account by submitting their registration information.
     *
     * @param userRequestDTO The UserRequestDTO containing the username, password, and other registration details.
     * This object is validated to ensure all required fields are present and correctly formatted.
     * @return A ResponseEntity containing the UserResponseDTO of the newly created user
     * and an HTTP status of 201 (Created) upon successful registration.
     */
    @Operation(summary = "Register User", description = "Registers a new user with the provided details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully, returns user details."), 
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid user data provided (missing fields, invalid format)."),
        @ApiResponse(responseCode = "409", description = "Conflict: User with this username/email already exists."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: Unmapped error.")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody @Parameter(description = "User registration details including username and password") UserRequestDTO userRequestDTO){
        UserResponseDTO userCreated = userService.createUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
    }

    /**
     * Allows an administrator to change the role of an existing user.
     * This endpoint is protected by Spring Security's `@PreAuthorize("hasRole('ADMIN')")`,
     * ensuring that only users with the 'ADMIN' role can access it.
     *
     * @param id The unique identifier (ID) of the user whose role is to be changed.
     * @param roleRequestDTO The RoleRequestDTO containing the new role information for the user.
     * @return A ResponseEntity containing the UserResponseDTO of the updated user
     * and an HTTP status of 200 (OK) upon successful role change.
     */
    @Operation(summary = "Change user role", description = "Allows an administrator to change an existing user's role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User role changed successfully."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid request body or target role provided."), 
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have administrative privileges to perform this action."), 
        @ApiResponse(responseCode = "404", description = "Not Found: User with the specified ID was not found."), 
        @ApiResponse(responseCode = "500", description = "Internal Server Error: Unmapped error.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id_user}/exchangepaper")
    public ResponseEntity<UserResponseDTO> exchangepaperUser(@PathVariable("id_user") @Parameter(description = "User ID") Long id, 
        @Valid @RequestBody @Parameter(description = "Details for changing the user's role, including the new role name.") RoleRequestDTO roleRequestDTO
    ){
        UserResponseDTO userChanged = userService.exchangepaperUser(id, roleRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(userChanged);
    }

    /**
     * Retrieves a paginated list of all registered users in the system.
     * This endpoint is restricted to users with the 'ADMIN' role for security purposes.
     *
     * @param pageable Pageable object containing pagination parameters such as page number, size, and sort order.
     * @return A ResponseEntity containing a PagedResponse of UserResponseDTO objects,
     * representing the paginated list of all users, along with an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all registered users. Only accessible by administrators.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of users."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have the necessary 'ADMIN' role to access this resource."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: Unmapped error.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponseDTO>> getAllUsers(@Parameter(description = "Pagination parameters (page, size, sort).") Pageable pageable) {
        PagedResponse<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
}