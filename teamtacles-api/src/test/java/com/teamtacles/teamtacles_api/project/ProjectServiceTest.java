package com.teamtacles.teamtacles_api.project;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.access.AccessDeniedException; 
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestPatchDTO;
import com.teamtacles.teamtacles_api.dto.request.TaskRequestDTO;
import com.teamtacles.teamtacles_api.dto.response.ProjectResponseDTO;
import com.teamtacles.teamtacles_api.exception.InvalidTaskStateException;
import com.teamtacles.teamtacles_api.exception.ResourceNotFoundException;
import com.teamtacles.teamtacles_api.mapper.PagedResponseMapper;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Role;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.RoleRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.service.ProjectService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ProjectServiceTest {
   
    @Autowired
    private RoleRepository roleRepository;

    @Mock
    private ProjectRepository projectRepository; 

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private ProjectService projectService;

    private ProjectRequestDTO projectRequestDTO;
    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User userADM;

    @BeforeEach
    void setUp() {
        // Criação de um ProjectRequestDTO e Users para serem usados ao decorrer dos testes
        projectRequestDTO = new ProjectRequestDTO();
        projectRequestDTO.setTitle("Project Backend API");
        projectRequestDTO.setDescription("A RESTful backend API built with Java for managing tasks, projects, and user access control.");

        user1 = new User(); user1.setUserId(1L);
        user2 = new User(); user2.setUserId(2L);
        user3 = new User(); user3.setUserId(3L);
        user4 = new User(); user4.setUserId(4L);
        userADM = new User(); userADM.setUserId(5L);

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName(ERole.ADMIN);
        userADM.setRoles(Set.of(adminRole));
    }

    @Test
    @DisplayName("Should create a project")
    void shouldCreateProjectWithValidData() {
        List<Long> listTeam = List.of(1L, 2L, 3L, 4L);
        projectRequestDTO.setTeam(listTeam);

        Project mappedProject = new Project();
        mappedProject.setTitle(projectRequestDTO.getTitle());
        mappedProject.setDescription(projectRequestDTO.getDescription());

        Project projectSaved = new Project();
        projectSaved.setTitle("Project Backend API");
        projectSaved.setDescription("A RESTful backend API built with Java for managing tasks, projects, and user access control.");
        projectSaved.setTeam(List.of(user1, user2, user3, user4));
        projectSaved.setCreator(user4);

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(projectSaved.getTitle());
        projectResponse.setDescription(projectSaved.getDescription());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user3));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user4));
        when(modelMapper.map(projectRequestDTO, Project.class)).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(projectSaved);
        when(modelMapper.map(projectSaved, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.createProject(projectRequestDTO, user4);

        assertNotNull(response);
        assert response.getTitle().equals("Project Backend API");
        assert response.getDescription().contains("RESTful backend API");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found")
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        List<Long> listTeam = List.of(1L, 2L, 3L);
        projectRequestDTO.setTeam(listTeam);

        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        // user4 não faz parte do time, portanto, uma exceção é lançada
        assertThrows(ResourceNotFoundException.class, () -> projectService.createProject(projectRequestDTO, user4));
    }
    
    @Test
    @DisplayName("Should return project by ID when it exists")
    void shouldReturnProjectByIdWhenExists() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setTitle(projectRequestDTO.getTitle());
        existingProject.setDescription(projectRequestDTO.getDescription());
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1, user2, user3));

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(existingProject.getTitle());
        projectResponse.setDescription(existingProject.getDescription());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(modelMapper.map(existingProject, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.getProjectById(projectId, user4);

        assertNotNull(response);
        assert response.getTitle().equals(projectRequestDTO.getTitle());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project is not found by ID")
    void shouldThrowResourceNotFoundExceptionWhenProjectNotFoundById() {
        Long projectId = 99L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(projectId, user4));
    }

    @Test
    @DisplayName("Should update project when user is the owner")
    void shouldUpdateProjectWhenUserIsOwner() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        Project projectSaved = new Project();
        projectSaved.setId(projectId);
        projectSaved.setCreator(user4);
        projectSaved.setTeam(List.of(user4, user1));
        projectSaved.setTitle(projectRequestDTO.getTitle());
        projectSaved.setDescription(projectRequestDTO.getDescription());

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(projectSaved.getTitle());
        projectResponse.setDescription(projectSaved.getDescription());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        doNothing().when(modelMapper).map(projectRequestDTO, existingProject);
        when(projectRepository.save(existingProject)).thenReturn(projectSaved);
        when(modelMapper.map(projectSaved, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.updateProject(projectId, projectRequestDTO, user4);

        assertNotNull(response);
        assert response.getTitle().equals(projectRequestDTO.getTitle());
        assert response.getDescription().equals(projectRequestDTO.getDescription());
    }

    @Test
    @DisplayName("Should update project when user is the ADM")
    void shouldUpdateProjectWhenUserIsADM() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        Project projectSaved = new Project();
        projectSaved.setId(projectId);
        projectSaved.setCreator(user4);
        projectSaved.setTeam(List.of(user4, user1));
        projectSaved.setTitle(projectRequestDTO.getTitle());
        projectSaved.setDescription(projectRequestDTO.getDescription());

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(projectSaved.getTitle());
        projectResponse.setDescription(projectSaved.getDescription());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        doNothing().when(modelMapper).map(projectRequestDTO, existingProject);
        when(projectRepository.save(existingProject)).thenReturn(projectSaved);
        when(modelMapper.map(projectSaved, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.updateProject(projectId, projectRequestDTO, userADM);

        assertNotNull(response);
        assert response.getTitle().equals(projectRequestDTO.getTitle());
        assert response.getDescription().equals(projectRequestDTO.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when non-owner or non-ADM tries to update project")
    void shouldThrowWhenNonOwnerOrADMUpdatesProject() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user1);
        existingProject.setTeam(List.of(user1, user2));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));

        // O criador desse projeto é o user1, então passandp CreateUser lançar a exceção
        assertThrows(AccessDeniedException.class, () -> projectService.updateProject(projectId, projectRequestDTO, user4));
    }
    

    @Test
    @DisplayName("Should partially update project when user is the owner")
    void shouldPartiallyUpdateProjectWhenUserIsOwner() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        ProjectRequestPatchDTO patchDTO = new ProjectRequestPatchDTO();
        patchDTO.setTitle(Optional.of("Updated Title"));
        patchDTO.setDescription(Optional.of("Updated Description"));

        Project projectSaved = new Project();
        projectSaved.setId(projectId);
        projectSaved.setCreator(user4);
        projectSaved.setTeam(List.of(user4, user1));
        projectSaved.setTitle(patchDTO.getTitle().get());
        projectSaved.setDescription(patchDTO.getDescription().get());

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(projectSaved.getTitle());
        projectResponse.setDescription(projectSaved.getDescription());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        doNothing().when(modelMapper).map(patchDTO, existingProject);
        when(projectRepository.save(existingProject)).thenReturn(projectSaved);
        when(modelMapper.map(projectSaved, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.partialUpdateProject(projectId, patchDTO, user4);

        assertNotNull(response);
        assert response.getTitle().equals(patchDTO.getTitle().get());
        assert response.getDescription().equals(patchDTO.getDescription().get());    
    }

    @Test
    @DisplayName("Should partially update project when user is the ADM")
    void shouldPartiallyUpdateProjectWhenUserIsADM() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        ProjectRequestPatchDTO patchDTO = new ProjectRequestPatchDTO();
        patchDTO.setTitle(Optional.of("Updated Title"));
        patchDTO.setDescription(Optional.of("Updated Description"));

        Project projectSaved = new Project();
        projectSaved.setId(projectId);
        projectSaved.setCreator(user4);
        projectSaved.setTeam(List.of(user4, user1));
        projectSaved.setTitle(patchDTO.getTitle().get());
        projectSaved.setDescription(patchDTO.getDescription().get());

        ProjectResponseDTO projectResponse = new ProjectResponseDTO();
        projectResponse.setTitle(projectSaved.getTitle());
        projectResponse.setDescription(projectSaved.getDescription());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        doNothing().when(modelMapper).map(patchDTO, existingProject);
        when(projectRepository.save(existingProject)).thenReturn(projectSaved);
        when(modelMapper.map(projectSaved, ProjectResponseDTO.class)).thenReturn(projectResponse);

        ProjectResponseDTO response = projectService.partialUpdateProject(projectId, patchDTO, userADM);

        assertNotNull(response);
        assert response.getTitle().equals(patchDTO.getTitle().get());
        assert response.getDescription().equals(patchDTO.getDescription().get());    
    }  

    @Test
    @DisplayName("Should throw exception when non-owner or non-ADM tries to partially update project")
    void shouldThrowWhenNonOwnerOrADMPartiallyUpdatesProject() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user1);
        existingProject.setTeam(List.of(user1, user2));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        ProjectRequestPatchDTO patchDTO = new ProjectRequestPatchDTO();
        patchDTO.setTitle(Optional.of("Updated Title"));
        patchDTO.setDescription(Optional.of("Updated Description"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        
        // O criador desse projeto é o user1, então passandp CreateUser lançar a exceção
        assertThrows(AccessDeniedException.class, () -> projectService.partialUpdateProject(projectId, patchDTO, user4));
    }

    @Test
    @DisplayName("Should delete project when user is the owner")
    void shouldDeleteProjectWhenUserIsOwner() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));

        projectService.deleteProject(projectId, user4);

        verify(projectRepository).delete(existingProject);
    }

    @Test
    @DisplayName("Should delete project when user is the ADM")
    void shouldDeleteProjectWhenUserIsADM() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user4);
        existingProject.setTeam(List.of(user4, user1));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));

        projectService.deleteProject(projectId, userADM);

        verify(projectRepository).delete(existingProject);
    }

    @Test
    @DisplayName("Should throw exception when non-owner or non-ADM tries to delete project")
    void shouldThrowWhenNonOwnerOrADMDeletesProject() {
        Long projectId = 1L;

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setCreator(user1);
        existingProject.setTeam(List.of(user1, user2));
        existingProject.setTitle("Old title");
        existingProject.setDescription("Old description");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        
        // O criador desse projeto é o user1, então passandp CreateUser lançar a exceção
        assertThrows(AccessDeniedException.class, () -> projectService.deleteProject(projectId, user4));
    }
}
