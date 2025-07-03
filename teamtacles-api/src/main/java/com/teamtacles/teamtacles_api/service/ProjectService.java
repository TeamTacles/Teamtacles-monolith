package com.teamtacles.teamtacles_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestPatchDTO;
import com.teamtacles.teamtacles_api.dto.response.ProjectResponseDTO;
import com.teamtacles.teamtacles_api.exception.InvalidTaskStateException;
import com.teamtacles.teamtacles_api.exception.ResourceNotFoundException;
import com.teamtacles.teamtacles_api.mapper.PagedResponseMapper;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Task;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.UserAuthenticated;
import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.model.enums.Status;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Service class responsible for handling business logic related to {Project entities
 * in the TeamTacles application. This includes creating, retrieving, updating, and deleting projects,
 * along with handling access control based on user roles and project ownership/membership.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-25
 */
@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper){
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    /**
     * Retrieves a paginated list of projects accessible by the authenticated user.
     * If the user has an ERole.ADMIN role, all projects in the system are returned.
     * Otherwise, only projects where the user is a team member are returned.
     *
     * @param pageable Pagination information (page number, page size, sorting).
     * @param userFromToken The authenticated  User retrieved from the security context.
     * @return A PagedResponse containing a page of ProjectResponseDTO objects.
     */
    public PagedResponse<ProjectResponseDTO> getAllProjects(Pageable pageable, User userFromToken){
        Page<Project> projectsPage;

        // se for ADM -> Listar tudo
        if(isADM(userFromToken)){
            projectsPage = projectRepository.findAll(pageable);
        }
        // Lista projetos em que eu participo
        else{
            projectsPage = projectRepository.findByTeam(userFromToken, pageable);
        }

        return pagedResponseMapper.toPagedResponse(projectsPage, ProjectResponseDTO.class);
    }

    /**
     * Retrieves a single project by its ID, ensuring the authenticated user has permission to view it.
     * Only the project creator, a team member, or an administrator can view a project.
     *
     * @param id The unique ID of the project to retrieve.
     * @param userFromToken The authenticated User attempting to view the project.
     * @return A ProjectResponseDTO representing the found project.
     */
    public ProjectResponseDTO getProjectById(@PathVariable Long id, User userFromToken){
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("project Not found."));
        // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureUserCanViewProject(project, userFromToken);

        return modelMapper.map(project, ProjectResponseDTO.class);
    }

    /**
     * Creates a new project in the system. The authenticated user initiating the creation
     * is automatically set as the project's creator and added to its team.
     * Additional team members are looked up by their IDs and added to the project's team.
     *
     * @param projectRequestDTO The ProjectRequestDTO containing the details for the new project.
     * @param userFromToken The authenticated User who is creating the project.
     * @return A ProjectResponseDTO representing the newly created project.
     */
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO, User userFromToken){
        // Busca o usuário criador do projeto
        User creatorUser = findUsers(userFromToken.getUserId());
         // Usar Set para evitar duplicatas
        List<User> teamList = new ArrayList<>();

        // Adiciona os demais membros da requisição
        for (Long userId : projectRequestDTO.getTeam()) {
            User user = findUsers(userId);
            teamList.add(user); // Se já estiver no Set, não será adicionado novamente
        }
        
        if (!teamList.contains(creatorUser)) {
            teamList.add(creatorUser);
        }
        
        Project convertedProject = modelMapper.map(projectRequestDTO, Project.class);

        convertedProject.setCreator(creatorUser);
        convertedProject.setTeam(teamList);

        Project projectCreated = projectRepository.save(convertedProject);
        return modelMapper.map(projectCreated, ProjectResponseDTO.class);
    }

    /**
     * Updates an existing project with the provided details.
     * Only the project creator or an administrator can fully update a project.
     * Preserves the existing project team and creator during the update.
     *
     * @param id The unique ID of the project to update.
     * @param projectRequestDTO The ProjectRequestDTO containing the updated project details.
     * @param userFromToken The authenticated User attempting to update the project.
     * @return A ProjectResponseDTO representing the updated project.
     */
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO projectRequestDTO, User userFromToken){
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("project Not found."));
            // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureUserCanAccessProject(project, userFromToken);

        modelMapper.map(projectRequestDTO, project);
        project.setId(id);
        project.setTeam(project.getTeam()); // preserva o time
        project.setCreator(project.getCreator()); // preserva o creator

        Project updatedProject = projectRepository.save(project);
        return modelMapper.map(updatedProject, ProjectResponseDTO.class);
    }

    /**
     * Partially updates an existing project with the provided details.
     * Only the project creator or an administrator can partially update a project.
     * Preserves the existing project team and creator during the update.
     *
     * @param id The unique ID of the project to partially update.
     * @param projectRequestPatchDTO The ProjectRequestPatchDT} containing the fields to be updated.
     * @param userFromToken The authenticated User attempting to partially update the project.
     * @return A ProjectResponseDTO representing the partially updated project.
     */
    public ProjectResponseDTO partialUpdateProject(Long id, ProjectRequestPatchDTO projectRequestPatchDTO, User userFromToken){
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("project Not found."));
            // Chama o método que verifica se o usuário é dono do projeto ou se é um administrador
        ensureUserCanAccessProject(project, userFromToken);

        modelMapper.map(projectRequestPatchDTO, project);
        project.setId(id);
        project.setTeam(project.getTeam()); // preserva o time
        project.setCreator(project.getCreator()); // preserva o creator

        Project updatedProject = projectRepository.save(project);
        return modelMapper.map(updatedProject, ProjectResponseDTO.class);
    }

    /**
     * Deletes a project from the system.
     * Only the project creator or an administrator can delete a project.
     *
     * @param id The unique ID of the project to delete.
     * @param userFromToken The authenticated User attempting to delete the project.
     */
    public void deleteProject(Long id, User userFromToken){
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("project Not found."));
            // Chama o método que verifica se o usuário é dono da tarefa ou se é um administrador
        ensureUserCanAccessProject(project, userFromToken);
        
        projectRepository.delete(project);
    }

    /**
     * Ensures that the given user has permission to view the specified project.
     * A user can view a project if they are an administrator or a member of the project's team.
     *
     * @param project The Project to check access for.
     * @param user The User attempting to access the project.
     */    
    public void ensureUserCanViewProject(Project project, User user) {
        if (!isADM(user) && !project.getTeam().stream().anyMatch(u -> u.getUserId().equals(user.getUserId()))){
            throw new AccessDeniedException("You do not have permission to access this resource.");    
        } 
    }

    private User findUsers(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("user Not found."));
        return user;
    }

    // Verificando se o usuário é admin
    private boolean isADM(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ADMIN));
    }

    // Validando se o usuário é dono do projeto, se ele não for adm, ele é proibido de criar, editar ou deletar projetos de outros usuários
    private void ensureUserCanAccessProject(Project project, User user) {
        if(!isADM(user) && !project.getCreator().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException (" FORBIDDEN - You do not have permission to modify this project."); 
        }
    }
}
