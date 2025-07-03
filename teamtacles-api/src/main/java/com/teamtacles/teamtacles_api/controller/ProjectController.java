package com.teamtacles.teamtacles_api.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestPatchDTO;
import com.teamtacles.teamtacles_api.dto.response.ProjectResponseDTO;
import com.teamtacles.teamtacles_api.dto.response.TaskResponseDTO;
import com.teamtacles.teamtacles_api.model.UserAuthenticated;
import com.teamtacles.teamtacles_api.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * REST controller for managing project-related operations in the TeamTacles application.
 * This controller provides endpoints for creating, retrieving, updating (full and partial),
 * and deleting projects. Access control is managed using Spring Security's `@AuthenticationPrincipal`
 * to retrieve the authenticated user and business logic defined in ProjectService.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-26
 */
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

    /**
     * Creates a new project in the system. The authenticated user making the request
     * is automatically assigned as the creator of the project.
     *
     * @param projectRequestDTO The ProjectRequestDTO containing the details for the new project.
     * This object is validated to ensure all required fields are present and correctly formatted.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user,
     * injected automatically by Spring Security. This parameter is hidden from Swagger documentation.
     * @return A ResponseEntity containing the ProjectResponseDTO of the newly created project
     * and an HTTP status of 201 (Created) upon successful creation.
     */
    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details. The authenticated user will be set as the project creator.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Project created successfully."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid project data provided (missing fields, invalid format)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject (@RequestBody @Valid @Parameter(description = "Details of the project to be created (name, description, team...).") ProjectRequestDTO projectRequestDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser
    ){
        ProjectResponseDTO projectResponseDTO = projectService.createProject(projectRequestDTO, authenticatedUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDTO);
    }

    /**
     * Retrieves a specific project by its unique identifier.
     * Access to a project is restricted: only users who are part of the project's team
     * or administrators can view it.
     *
     * @param id The unique ID of the project to retrieve.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A {@link ResponseEntity} containing the ProjectResponseDTO of the found project
     * and an HTTP status of 200 (OK) if the user has permission.
     */
    @Operation(summary = "Get project by id", description = "Retrieves a specific project by its ID. Users can only view projects when they're in the team.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated list of projects."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User not in the team."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser){
        return ResponseEntity.ok(projectService.getProjectById(id, authenticatedUser.getUser()));
    }

    /**
     * Retrieves a paginated list of projects.
     * If the authenticated user is an administrator, all projects in the system are returned.
     * Otherwise, only projects that the user is associated with (as a team member) are returned.
     *
     * @param pageable Pageable object containing pagination parameters (page number, size, sort).
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing a PagedResponse of {ProjectResponseDTO objects,
     * representing the paginated list of projects, and an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get all projects", description = "Retrieves a paginated list of all projects. Users can typically only view projects they are associated with.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the paginated list of projects."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<ProjectResponseDTO>> getAllProjects(@Parameter(description = "Pagination parameters (page, size, sort).") Pageable pageable, 
        @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser
    ){
        PagedResponse projectsPage = projectService.getAllProjects(pageable, authenticatedUser.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(projectsPage);
    }  

    /**
     * Updates an existing project fully with the provided details.
     * This operation requires the authenticated user to be either the project's creator or an administrator.
     *
     * @param id The unique ID of the project to update.
     * @param projectRequestDTO The ProjectRequestDTO containing the complete updated details for the project.
     * This object is validated.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing the ProjectResponseDTO of the updated project
     * and an HTTP status of 200 (OK) upon successful update.
     */
    @Operation(summary = "Update an existing project", description = "Updates an existing project identified by its ID. Only the project creator or an administrator may update the project.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully, returns the updated project details."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid project data provided (missing fields, invalid format)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project with the specified ID was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PutMapping("/{id}") 
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable @Parameter(description = "Project ID") Long id, 
        @Valid @RequestBody @Parameter(description = "Updated details for the project.") ProjectRequestDTO projectRequestDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser
    ){
        ProjectResponseDTO responseDTO = projectService.updateProject(id, projectRequestDTO, authenticatedUser.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    /**
     * Partially updates an existing project with the provided details.
     * This operation allows updating specific fields of a project without replacing the entire resource.
     * It requires the authenticated user to be either the project's creator or an administrator.
     *
     * @param id The unique ID of the project to partially update.
     * @param projectRequestPatchDTO The ProjectRequestPatchDTO containing the fields to be partially updated.
     * This object is validated.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity containing the rojectResponseDTO of the partially updated project
     * and an HTTP status of 200 (OK) upon successful update.
     */
    @Operation(summary = "Partially update a project", description = "Updates one or more specific fields of an existing project identified by its ID. Only the project creator or an administrator may perform this partial update.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully, returns the updated project details."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Invalid project data provided (missing fields, invalid format)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to update this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project with the specified ID was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> partialUpdateProject(@PathVariable @Parameter(description = "Project ID") Long id, 
        @Valid @RequestBody @Parameter(description = "Fields to be partially updated for the project.") ProjectRequestPatchDTO projectRequestPatchDTO, 
        @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser
    ){
        ProjectResponseDTO responseDTO = projectService.partialUpdateProject(id, projectRequestPatchDTO, authenticatedUser.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    /**
     * Deletes an existing project from the system.
     * This operation requires the authenticated user to be either the project's creator or an administrator.
     *
     * @param id The unique ID of the project to delete.
     * @param authenticatedUser The UserAuthenticated object representing the currently authenticated user.
     * @return A ResponseEntity with no content (HTTP status 204 No Content) upon successful deletion.
     */
    @Operation(summary = "Delete a project", description = "Deletes an existing project identified by its ID. Only the project creator or an administrator may delete the project.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully (No Content)."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required or invalid token."),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have permission to delete this project."),
        @ApiResponse(responseCode = "404", description = "Not Found: Project with the specified ID was not found."),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: An unexpected error occurred.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Parameter(description = "Project ID") Long id, 
        @Parameter(hidden = true) @AuthenticationPrincipal UserAuthenticated authenticatedUser
    ){
        projectService.deleteProject(id, authenticatedUser.getUser());
        return ResponseEntity.noContent().build();
    }
}