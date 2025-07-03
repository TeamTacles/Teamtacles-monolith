package com.teamtacles.teamtacles_api.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;
import com.teamtacles.teamtacles_api.dto.request.TaskRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.TaskRequestPatchDTO;
import com.teamtacles.teamtacles_api.dto.response.ProjectResponseFilteredDTO;
import com.teamtacles.teamtacles_api.dto.response.TaskResponseDTO;
import com.teamtacles.teamtacles_api.exception.InvalidTaskStateException;
import com.teamtacles.teamtacles_api.exception.ResourceNotFoundException;
import com.teamtacles.teamtacles_api.mapper.PagedResponseMapper;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Role;
import com.teamtacles.teamtacles_api.model.Task;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.TaskRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.service.ProjectService;
import com.teamtacles.teamtacles_api.service.TaskService;
import com.teamtacles.teamtacles_api.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.teamtacles.teamtacles_api.dto.page.PagedResponse;
import org.springframework.security.access.AccessDeniedException; 
import com.teamtacles.teamtacles_api.dto.response.TaskResponseFilteredDTO;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TaskService taskService;

    private User adminUser;
    private User normalUser;
    private User responsibleUser;
    private User otherUser;
    private Project testProject; 
    private Role adminRoleInstance;
    private Role userRoleInstance;
    private Task existingTask; 

    @BeforeEach 
    void setUp() {

        // Configuração dos papéis
        adminRoleInstance = new Role();
        adminRoleInstance.setId(1L); 
        adminRoleInstance.setRoleName(ERole.ADMIN);

        userRoleInstance = new Role();
        userRoleInstance.setId(2L);
        userRoleInstance.setRoleName(ERole.USER);

        // Configuração do Usuário Admin
        adminUser = new User();
        adminUser.setUserId(1L);      
        adminUser.setUserName("admin"); 
        adminUser.setRoles(Set.of(adminRoleInstance));

        // Configuração do Usuário Normal
        normalUser = new User();
        normalUser.setUserId(2L);
        normalUser.setUserName("normaluser");
        normalUser.setRoles(Set.of(userRoleInstance));

        // Configuração do Usuário Responsável
        responsibleUser = new User();
        responsibleUser.setUserId(3L);
        responsibleUser.setUserName("responsibleuser");
        responsibleUser.setRoles(Set.of(userRoleInstance));

        // Configuração do Outro Usuário
        otherUser = new User();
        otherUser.setUserId(4L);
        otherUser.setUserName("otheruser");
        otherUser.setRoles(Set.of(userRoleInstance));

        // Configuração do Projeto de Teste

        testProject = new Project();
        testProject.setId(100L);
        testProject.setTitle("Test Project");
        testProject.setCreator(normalUser);
        testProject.setTeam(List.of(normalUser, responsibleUser, adminUser));

        // Configurando Tarefa existente
        existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Existing Task");
        existingTask.setProject(testProject);
        existingTask.setOwner(normalUser); // O dono da tarefa é o usuário normal
        existingTask.setStatus(Status.INPROGRESS);
        existingTask.setUsersResponsability(List.of(responsibleUser));
        existingTask.setDueDate(LocalDateTime.now().plusDays(5));
        

    }

    @Test
    @DisplayName("1.1: Should create a task successfully when data is valid")
    void createTask_shouldCreateTaskSuccessfully_whenDataIsValid() {

        // Arrange
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New Task");
        requestDTO.setDescription("Task description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(7));
        requestDTO.setUsersResponsability(List.of(responsibleUser.getUserId()));

        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userRepository.findById(normalUser.getUserId())).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(responsibleUser.getUserId())).thenReturn(Optional.of(responsibleUser));

        Task taskToBeMapped = new Task(); 
        taskToBeMapped.setTitle(requestDTO.getTitle());
        taskToBeMapped.setDescription(requestDTO.getDescription());
        taskToBeMapped.setDueDate(requestDTO.getDueDate());
        when(modelMapper.map(requestDTO, Task.class)).thenReturn(taskToBeMapped);

        // Configuração do que o taskRepository deve retornar
        Task savedTaskEntity = new Task();
        savedTaskEntity.setId(1L);
        savedTaskEntity.setTitle(requestDTO.getTitle());
        savedTaskEntity.setDescription(requestDTO.getDescription());
        savedTaskEntity.setDueDate(requestDTO.getDueDate());
        savedTaskEntity.setProject(testProject);
        savedTaskEntity.setOwner(normalUser);
        savedTaskEntity.setStatus(Status.TODO); 
        savedTaskEntity.setUsersResponsability(List.of(responsibleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTaskEntity);

        // Configuração do que o modelMapper deve retornar
        TaskResponseDTO expectedResponseDTO = new TaskResponseDTO();
        expectedResponseDTO.setId(savedTaskEntity.getId());
        expectedResponseDTO.setTitle(savedTaskEntity.getTitle());

        when(modelMapper.map(savedTaskEntity, TaskResponseDTO.class)).thenReturn(expectedResponseDTO);

        // Act
        TaskResponseDTO actualResponseDTO = taskService.createTask(testProject.getId(), requestDTO, normalUser);

        // Assert
        assertNotNull(actualResponseDTO, "The response DTO should not be null.");
        assertEquals(expectedResponseDTO.getId(), actualResponseDTO.getId(), "Response DTO ID should match.");
        assertEquals(expectedResponseDTO.getTitle(), actualResponseDTO.getTitle(), "Response DTO title should match.");

        verify(taskRepository, times(1)).save(any(Task.class));

        // Usando o ArgumentCaptor para capturar o Task passado para o save  
        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskArgumentCaptor.capture()); 
        Task taskPassedToSave = taskArgumentCaptor.getValue();     

        assertEquals(Status.TODO, taskPassedToSave.getStatus(), "The initial status of the task should be TODO.");
        assertEquals(normalUser, taskPassedToSave.getOwner(), "The owner of the task should be the user who created it.");
        assertEquals(testProject, taskPassedToSave.getProject(), "The task should be associated with the correct project.");
        assertTrue(taskPassedToSave.getUsersResponsability().contains(responsibleUser), "The responsible user should be in the list.");
        assertEquals(requestDTO.getTitle(), taskPassedToSave.getTitle(), "The title of the task passed to save should match the DTO.");
    }

    @Test
    @DisplayName("1.2: Should throw ResourceNotFoundException when project does not exist")
    void createTask_shouldThrowResourceNotFoundException_whenProjectNotFound() {

        // Arrange
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task for Nonexistent Project");
        requestDTO.setDescription("This task should not be created.");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(7));
        requestDTO.setUsersResponsability(List.of(responsibleUser.getUserId()));

        // Simulando id de projeto inexistente
        Long nonexistentProjectId = 999L;

        when(projectRepository.findById(nonexistentProjectId)).thenReturn(Optional.empty());
            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                taskService.createTask(nonexistentProjectId, requestDTO, normalUser);
            });
            verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("1.3: Should throw ResourceNotFoundException_whenResponsibleUserNotFound") 
    void createTask_shouldThrowResourceNotFoundException_whenResponsibleUserNotFound() {
        //Arrange
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task with Nonexistent Responsible User");
        requestDTO.setDescription("This task should not be created.");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(7));

        Long existentResponsibleUserId = responsibleUser.getUserId();
        Long nonexistentResponsibleUserId = 999L;
        requestDTO.setUsersResponsability(List.of(existentResponsibleUserId, nonexistentResponsibleUserId));

        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userRepository.findById(normalUser.getUserId())).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(existentResponsibleUserId)).thenReturn(Optional.of(responsibleUser));
        when(userRepository.findById(nonexistentResponsibleUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(testProject.getId(), requestDTO, normalUser);
        });
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("1.4: Should throw AccessDeniedException when user creating task cannot view project")
    void createTask_shouldThrowAccessDeniedException_whenUserCannotViewProject() {
        // Arrange
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task in inaccessible project");
        requestDTO.setDescription("This task should not be created.");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(7));
        requestDTO.setUsersResponsability(List.of(responsibleUser.getUserId())); 

        Long projectId = testProject.getId();
        User userAttemptingCreation = otherUser; 

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(userAttemptingCreation.getUserId())).thenReturn(Optional.of(userAttemptingCreation));
        doThrow(new AccessDeniedException("User does not have permission to view this project to create a task."))
            .when(projectService).ensureUserCanViewProject(eq(testProject), eq(userAttemptingCreation));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.createTask(projectId, requestDTO, userAttemptingCreation);
        });
        
        assertEquals("User does not have permission to view this project to create a task.", exception.getMessage());
        
        verify(projectRepository).findById(projectId); 
        verify(userRepository).findById(userAttemptingCreation.getUserId());
        verify(projectService).ensureUserCanViewProject(eq(testProject), eq(userAttemptingCreation)); 
        verify(userRepository, never()).findById(responsibleUser.getUserId()); 
        verify(taskRepository, never()).save(any(Task.class)); 
    }

    @Test
    @DisplayName("2.1: Admin should get any task")
    void getTasksById_shouldReturnTask_whenUserIsAdmin() {
        //Arrange
        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        //Mock model mapper
        TaskResponseDTO expectedResponseDTO = new TaskResponseDTO();
        expectedResponseDTO.setId(existingTask.getId());
        expectedResponseDTO.setTitle(existingTask.getTitle());
        when(modelMapper.map(existingTask, TaskResponseDTO.class)).thenReturn(expectedResponseDTO);
        // Act
        TaskResponseDTO actualResponseDTO = taskService.getTasksById(testProject.getId(), existingTask.getId(), adminUser);
        // Assert
        assertNotNull(actualResponseDTO, "Response DTO should not be null for admin.");
        assertEquals(expectedResponseDTO.getId(), actualResponseDTO.getId(), "Task ID should match.");
        assertEquals(expectedResponseDTO.getTitle(), actualResponseDTO.getTitle(), "Task title should match.");
        verify(taskRepository, times(1)).findById(existingTask.getId());
        verify(modelMapper, times(1)).map(existingTask, TaskResponseDTO.class);
    }

    @Test
    @DisplayName("2.2: Owner should get their own task by ID successfully")
    void getTasksById_shouldReturnTask_whenUserIsOwner() {
        // Arrange
        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        // Mock model mapper
        TaskResponseDTO expectedResponseDTO = new TaskResponseDTO();
        expectedResponseDTO.setId(existingTask.getId());
        expectedResponseDTO.setTitle(existingTask.getTitle());
        when(modelMapper.map(existingTask, TaskResponseDTO.class)).thenReturn(expectedResponseDTO);

        //Act
        TaskResponseDTO actualResponseDTO = taskService.getTasksById(testProject.getId(), existingTask.getId(), normalUser);

        // Assert
        assertNotNull(actualResponseDTO, "Response DTO should not be null for owner.");
        assertEquals(expectedResponseDTO.getId(), actualResponseDTO.getId(), "Task ID should match.");
        assertEquals(expectedResponseDTO.getTitle(), actualResponseDTO.getTitle(), "Task title should match.");
        verify(taskRepository, times(1)).findById(existingTask.getId());
        verify(modelMapper, times(1)).map(existingTask, TaskResponseDTO.class);


    }

    @Test
    @DisplayName("2.3: Responsible user should get task by ID successfully")
    void getTasksById_shouldReturnTask_whenUserIsResponsible() {
        //Arrange
        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        // Mock model mapper
        TaskResponseDTO expectedResponseDTO = new TaskResponseDTO();
        expectedResponseDTO.setId(existingTask.getId());
        expectedResponseDTO.setTitle(existingTask.getTitle());
        when(modelMapper.map(existingTask, TaskResponseDTO.class)).thenReturn(expectedResponseDTO);

        //act
        TaskResponseDTO actualResponseDTO = taskService.getTasksById(testProject.getId(), existingTask.getId(), responsibleUser);
        // Assert
        assertNotNull(actualResponseDTO, "Response DTO should not be null for responsible user.");
        assertEquals(expectedResponseDTO.getId(), actualResponseDTO.getId(), "Task ID should match.");
        assertEquals(expectedResponseDTO.getTitle(), actualResponseDTO.getTitle(), "Task title should match.");

        verify(taskRepository, times(1)).findById(existingTask.getId());
        verify(modelMapper, times(1)).map(existingTask, TaskResponseDTO.class);

    }

    @Test
    @DisplayName("2.4: Should throw ResourceNotFoundException when task ID does not exist")
    void getTasksById_shouldThrowResourceNotFoundException_whenTaskNotFound() {

        //Arrange
        Long nonexistentTaskId = 999L;

        when(taskRepository.findById(nonexistentTaskId)).thenReturn(Optional.empty());
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksById(testProject.getId(), nonexistentTaskId, normalUser);
        });
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("2.5: Should throw ResourceNotFoundException when task does not belong to the specified project") 
    void getTasksById_shouldThrowResourceNotFoundException_whenTaskDoesNotBelongToProject() {
        //usando a existingTask que foi criada no setUp, mas com um projeto diferente
        Long differentProjectId = 777L; 

        // a tarefa deve ser encontrada, pois o problema não é a tarefa em si, mas sim o projeto ao qual ela pertence
        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksById(differentProjectId, existingTask.getId(), normalUser);
        });

        verify(modelMapper, never()).map(any(), any());


    }

        @Test
        @DisplayName("2.6: Should throw AccessDeniedException when unauthorized user tries to access task by ID")
        void getTasksById_shouldThrowAccessDeniedException_whenUnauthorizedUserIsNotAuthorized() { 
        // Arrange
        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));
        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> { 
            taskService.getTasksById(testProject.getId(), existingTask.getId(), otherUser);
        });
        //verificando q o modelMapper não foi chamado, pois o usuário não tem permissão para acessar a tarefa
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("3.1: Admin Should get all tasks for a specific user in a specific project")
    void getAllTasksFromUserInProject_shouldReturnPagedTasks_whenUserIsAdmin() {
        //Arrange
        Long projectIdToSearch = testProject.getId();
        Long  userIdToSearchTasksFor = responsibleUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10); 

        // Simulando que o projeto e o usu existam
        when(projectRepository.findById(projectIdToSearch)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(userIdToSearchTasksFor)).thenReturn(Optional.of(responsibleUser));

        // Simulando que o repositório de tarefas retorna uma lista de tarefas
        List<Task> tasksForUser = List.of(existingTask);
        Page<Task> tasksPageFromRepo = new PageImpl<>(tasksForUser, pageable, tasksForUser.size());
    
        when(taskRepository.findByProjectIdAndUsersResponsabilityId(projectIdToSearch, userIdToSearchTasksFor, pageable)).thenReturn(tasksPageFromRepo);
        
        // Simulando mapeamento da pagina de entidades para pagina de DTOs
        TaskResponseDTO taskDto = new TaskResponseDTO();
        taskDto.setId(existingTask.getId());
        taskDto.setTitle(existingTask.getTitle());

        List<TaskResponseDTO> dtoList= List.of(taskDto);
        PagedResponse<TaskResponseDTO> expectedPagedResponse = new PagedResponse<>(dtoList, tasksPageFromRepo.getNumber(), 
            tasksPageFromRepo.getSize(), tasksPageFromRepo.getTotalElements(), tasksPageFromRepo.getTotalPages(), tasksPageFromRepo.isLast());
            when(pagedResponseMapper.toPagedResponse(tasksPageFromRepo, TaskResponseDTO.class)).thenReturn(expectedPagedResponse);

            //Act
            PagedResponse<TaskResponseDTO> actualPagedResponse = taskService.getAllTasksFromUserInProject(
                pageable, projectIdToSearch, userIdToSearchTasksFor, adminUser);

            //Assert 
            assertNotNull(actualPagedResponse, "The paged response should not be null.");
            assertEquals(expectedPagedResponse.getTotalElements(), actualPagedResponse.getTotalElements(), "Total elements should match.");
            assertEquals(1, actualPagedResponse.getContent().size(), "Content size should match the number of tasks returned.");
            assertEquals(existingTask.getTitle(), actualPagedResponse.getContent().get(0).getTitle(), "Task titles should match.");

            verify(projectRepository, times(1)).findById(projectIdToSearch);
            verify(userRepository, times(1)).findById(userIdToSearchTasksFor);
            verify(taskRepository, times(1)).findByProjectIdAndUsersResponsabilityId(projectIdToSearch, userIdToSearchTasksFor, pageable);
            verify(pagedResponseMapper, times(1)).toPagedResponse(tasksPageFromRepo, TaskResponseDTO.class);

    }

    @Test
    @DisplayName("3.2: Should throw AccessDeniedException when a non-admin user tries to get tasks from another user in a project")
    void getAllTasksFromUserInProject_shouldThrowAccessDeniedException_whenUserIsNotAdmin() {
    //Arrange
    Long projectIdToSearch = testProject.getId();
    Long userIdToSearchTasksFor = responsibleUser.getUserId();
    Pageable pageable = PageRequest.of(0, 10);

    when(projectRepository.findById(projectIdToSearch)).thenReturn(Optional.of(testProject));
    when(userRepository.findById(userIdToSearchTasksFor)).thenReturn(Optional.of(responsibleUser));

    //Act & Assert
    AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
        taskService.getAllTasksFromUserInProject(pageable, projectIdToSearch, userIdToSearchTasksFor, normalUser);
    });

    assertEquals("FORBIDDEN - You do not have permission to access this user's tasks.", exception.getMessage());

    verify(projectRepository, times(1)).findById(projectIdToSearch);
    verify(userRepository, times(1)).findById(userIdToSearchTasksFor);
    verify(taskRepository, never()).findByProjectIdAndUsersResponsabilityId(anyLong(), anyLong(), any(Pageable.class));
    verify(pagedResponseMapper, never()).toPagedResponse(any(), any());
}

    @Test
    @DisplayName("3.3: Should throw ResourceNotFoundException when project is not found (admin acess)")
    void getAllTasksFromUserInProject_shouldThrowResourceNotFoundException_whenProjectNotFoundForAdmin() {
        //Arrange
        Long nonexistentProjectId = 888L;
        Long userIdToSearchTasksFor = responsibleUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findById(nonexistentProjectId)).thenReturn(Optional.empty());
        //act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getAllTasksFromUserInProject(pageable, nonexistentProjectId, userIdToSearchTasksFor, adminUser);
        });

        verify(userRepository, never()).findById(anyLong()); 
        verify(taskRepository, never()).findByProjectIdAndUsersResponsabilityId(anyLong(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(), any());
    }

    @Test
    @DisplayName("3.4: Should  throw ResourceNotFoundException when target user for task search is not found (admin acess)")
    void getAllTasksFromUserInProject_shouldThrowResourceNotFoundException_whenTargetUserNotFoundForAdmin() {
        //Arrange

        Long projectIdToSearch = testProject.getId();
        Long nonexistentUseridToSearchTasksFor = 997L;
        Pageable pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(projectIdToSearch)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(nonexistentUseridToSearchTasksFor)).thenReturn(Optional.empty());

        //act & assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getAllTasksFromUserInProject(pageable, projectIdToSearch, nonexistentUseridToSearchTasksFor, adminUser);
        });
        verify(taskRepository, never()).findByProjectIdAndUsersResponsabilityId(anyLong(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(), any());
        verify(projectRepository, times(1)).findById(projectIdToSearch);
        
    }

    @Test
    @DisplayName("3.5: Should return an empty page when target user has no tasks in the project (admin acess)")
    void getAllTasksFromUserInProject_shouldReturnEmptyPage_whenTargetUserHasNoTasks() {
        //Arrange

        Long projectIdToSearch = testProject.getId();
        Long userIdToSearchTasksFor = responsibleUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findById(projectIdToSearch)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(userIdToSearchTasksFor)).thenReturn(Optional.of(responsibleUser));

        List<Task> emptyTaskList = List.of(); // Lista vazia para simular que o usuário não tem tarefas
        Page<Task> emptyTasksPageFromRepo = new PageImpl<>(emptyTaskList, pageable, 0);
        when(taskRepository.findByProjectIdAndUsersResponsabilityId(projectIdToSearch, userIdToSearchTasksFor, pageable)).thenReturn(emptyTasksPageFromRepo);

        List<TaskResponseDTO> emptyDtoList = List.of(); // Lista vazia de DTOs
        PagedResponse<TaskResponseDTO> expectedEmptyPagedResponse = new PagedResponse<>(emptyDtoList, emptyTasksPageFromRepo.getNumber(), emptyTasksPageFromRepo.getSize(), emptyTasksPageFromRepo.getTotalElements(), emptyTasksPageFromRepo.getTotalPages(), emptyTasksPageFromRepo.isLast());

        when(pagedResponseMapper.toPagedResponse(emptyTasksPageFromRepo, TaskResponseDTO.class)).thenReturn(expectedEmptyPagedResponse);

        //Act
        PagedResponse<TaskResponseDTO> actualPagedResponse = taskService.getAllTasksFromUserInProject(
            pageable, projectIdToSearch, userIdToSearchTasksFor, adminUser); //adm fazendo a requisição
        //Assert
        assertNotNull(actualPagedResponse, "PagedResponse should not be null even if content is empty.");
        assertTrue(actualPagedResponse.getContent().isEmpty(), "Content list should be empty.");
        assertEquals(0, actualPagedResponse.getTotalElements(), "Total elements should be 0."); 
        
        verify(projectRepository, times(1)).findById(projectIdToSearch);
        verify(userRepository, times(1)).findById(userIdToSearchTasksFor);
        verify(taskRepository, times(1)).findByProjectIdAndUsersResponsabilityId(projectIdToSearch, userIdToSearchTasksFor, pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(emptyTasksPageFromRepo, TaskResponseDTO.class);
    }

    @Test
    @DisplayName("4.1: Admin should get all tasks (no filters) successfully")
    void getAllTasksFiltered_shouldReturnAllTasks_whenAdminAndNoFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5); 
        User userMakingRequest = adminUser; // Admin fazendo a requisição

        List<Task> tasksFromRepo = List.of(existingTask); 
        Page<Task> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());
        
        // nesse cenário o status, dueDate e projectId são null
        when(taskRepository.findTasksFiltered(null, null, null, pageable))
            .thenReturn(taskPageFromRepo);

        TaskResponseFilteredDTO taskFilteredDto = new TaskResponseFilteredDTO();
        taskFilteredDto.setId(existingTask.getId());
        taskFilteredDto.setTitle(existingTask.getTitle());

        List<TaskResponseFilteredDTO> dtoList = List.of(taskFilteredDto);
        PagedResponse<TaskResponseFilteredDTO> expectedPagedResponse = new PagedResponse<>(
            dtoList,
            taskPageFromRepo.getNumber(),
            taskPageFromRepo.getSize(),
            taskPageFromRepo.getTotalElements(),
            taskPageFromRepo.getTotalPages(),
            taskPageFromRepo.isLast()
        );
        when(pagedResponseMapper.toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class))
            .thenReturn(expectedPagedResponse);

        // Act
        PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
            null,       
            null,      
            null,       
            pageable,
            userMakingRequest
        );

        // Assert
        assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
        assertEquals(expectedPagedResponse.getTotalElements(), actualPagedResponse.getTotalElements(), "Total elements should match.");
        assertFalse(actualPagedResponse.getContent().isEmpty(), "Content should not be empty if tasks were returned.");
        assertEquals(existingTask.getTitle(), actualPagedResponse.getContent().get(0).getTitle(), "Task title should match.");

        verify(projectRepository, never()).findById(anyLong());
        verify(projectService, never()).ensureUserCanViewProject(any(Project.class), any(User.class));
        verify(taskRepository, times(1)).findTasksFiltered(null, null, null, pageable);
        // Garante que o outro método de filtro do user comum não foi chamado
        verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, times(1)).toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class);
    }

    @Test
    @DisplayName("4.2:  Admin shouldget tasks with all filters applied")
        void getAllTasksFiltered_shouldReturnFilteredTasks_whenAdminAndAllFiltersApplied() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        User userMakingRequest = adminUser;
        String statusFilterString = "TODO"; 
        Status expectedStatusEnum = Status.TODO; // Convertendo o filtro P ENUM
        LocalDateTime dueDateFilter = LocalDateTime.now().plusDays(10);
        Long projectIdFilter = testProject.getId();

        when(projectRepository.findById(projectIdFilter)).thenReturn(Optional.of(testProject));
        doNothing().when(projectService).ensureUserCanViewProject(testProject, userMakingRequest); // para adm esse método não deve lançar exceção

        List<Task> tasksFromRepo = List.of(existingTask); 
        Page<Task> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());

        when(taskRepository.findTasksFiltered(expectedStatusEnum, dueDateFilter, projectIdFilter, pageable)).thenReturn(taskPageFromRepo);

        TaskResponseFilteredDTO taskFilteredDto = new TaskResponseFilteredDTO();
        taskFilteredDto.setId(existingTask.getId());
        taskFilteredDto.setTitle(existingTask.getTitle());
        taskFilteredDto.setStatus(expectedStatusEnum); 

        List<TaskResponseFilteredDTO> dtoList = List.of(taskFilteredDto);
        PagedResponse<TaskResponseFilteredDTO> expectedPagedResponse = new PagedResponse<>(
            dtoList,
            taskPageFromRepo.getNumber(),
            taskPageFromRepo.getSize(),
            taskPageFromRepo.getTotalElements(),
            taskPageFromRepo.getTotalPages(),
            taskPageFromRepo.isLast()
        );
        when(pagedResponseMapper.toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class)).thenReturn(expectedPagedResponse);

        //act 

        PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
            statusFilterString,
            dueDateFilter,
            projectIdFilter,
            pageable,
            userMakingRequest
        );

        // Assert
        assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
        assertEquals(expectedPagedResponse.getTotalElements(), actualPagedResponse.getTotalElements());
        assertFalse(actualPagedResponse.getContent().isEmpty());
        assertEquals(existingTask.getTitle(), actualPagedResponse.getContent().get(0).getTitle());

        verify(projectRepository, times(1)).findById(projectIdFilter);
        verify(projectService, times(1)).ensureUserCanViewProject(testProject, userMakingRequest);
        verify(taskRepository, times(1)).findTasksFiltered(expectedStatusEnum, dueDateFilter, projectIdFilter, pageable);
        verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, times(1)).toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class);
    }

    @Test
    @DisplayName("4.3: Admin should get ResourceNotFoundException when filtering by a non-existent projectId")
        void getAllTasksFiltered_shouldThrowResourceNotFoundException_whenAdminFiltersByNonExistentProjectId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        User userMakingRequest = adminUser;

        Long nonExistentProjectId = 999L; // ID de projeto inexistente
        String statusFilterString = "TODO";
        LocalDateTime dueDateFilter = null;

        when(projectRepository.findById(nonExistentProjectId)).thenReturn(Optional.empty());

        //act e Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getAllTasksFiltered(
                statusFilterString,
                dueDateFilter,
                nonExistentProjectId,
                pageable,
                userMakingRequest
            );
        });

        verify(projectRepository, times(1)).findById(nonExistentProjectId);
        verify(projectService, never()).ensureUserCanViewProject(any(Project.class), any(User.class));
        verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
        verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(), any());
}

    @Test
    @DisplayName("4.4: Admin should get IllegalArgumentException whenwhen filtering by an invalid status string")
    void getAllTasksFiltered_shouldThrowIllegalArgumentException_whenAdminFiltersByInvalidStatusString() {

        //Arrange
        Pageable pageable = PageRequest.of(0, 5);
        User userMakingRequest = adminUser;

        String invalidStatusString = "INVALID_STATUS"; // Status inválido
        LocalDateTime dueDateFilter = null;
        Long projectIdFilter = null;

        //act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getAllTasksFiltered(
                invalidStatusString,
                dueDateFilter,
                projectIdFilter,
                pageable,
                userMakingRequest
            );
        });
        verify(projectRepository, never()).findById(anyLong());
        verify(projectService, never()).ensureUserCanViewProject(any(Project.class), any(User.class));
        verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
        verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class));
        verify(pagedResponseMapper, never()).toPagedResponse(any(), any());

}

    @Test
    @DisplayName("4.5: Normal user should get their tasks (no filters) using getAllTasksFiltered")
    void getAllTasksFiltered_shouldReturnUserTasks_whenNormalUserAndNoFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User userMakingRequest = normalUser; // Usuário normal fazendo a requisição

        List<Task> userTasksFromRepo = List.of(existingTask);
        Page<Task> taskPageFromRepo = new PageImpl<>(userTasksFromRepo, pageable, userTasksFromRepo.size());

        when(taskRepository.findTasksFilteredByUser(null, null, null, userMakingRequest.getUserId(), pageable))
            .thenReturn(taskPageFromRepo);

        TaskResponseFilteredDTO filteredDto = new TaskResponseFilteredDTO();
        filteredDto.setId(existingTask.getId());
        filteredDto.setTitle(existingTask.getTitle());

        List<TaskResponseFilteredDTO> dtoList = List.of(filteredDto);
        PagedResponse<TaskResponseFilteredDTO> expectedPagedResponse = new PagedResponse<>(
            dtoList,
            taskPageFromRepo.getNumber(),
            taskPageFromRepo.getSize(),
            taskPageFromRepo.getTotalElements(),
            taskPageFromRepo.getTotalPages(),
            taskPageFromRepo.isLast()
        );
        when(pagedResponseMapper.toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class))
            .thenReturn(expectedPagedResponse);

        // Act
        PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
            null,       
            null,      
            null,       
            pageable,
            userMakingRequest 
        );

        // Assert
        assertNotNull(actualPagedResponse, "PagedResponse should not be null.");
        assertEquals(expectedPagedResponse.getTotalElements(), actualPagedResponse.getTotalElements(), "Total elements should match.");
        assertFalse(actualPagedResponse.getContent().isEmpty(), "Content list should not be empty if tasks are returned.");
        assertEquals(existingTask.getTitle(), actualPagedResponse.getContent().get(0).getTitle(), "Task title should match.");

        verify(projectRepository, never()).findById(anyLong());
        verify(projectService, never()).ensureUserCanViewProject(any(Project.class), any(User.class));
        verify(taskRepository, times(1)).findTasksFilteredByUser(null, null, null, userMakingRequest.getUserId(), pageable);
        verify(taskRepository, never()).findTasksFiltered(any(), any(), any(), any(Pageable.class));
        verify(pagedResponseMapper, times(1)).toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class);
    }

    @Test
    @DisplayName("4.6: Normal user should get their tasks with filters applied using getAllTasksFiltered")
    void getAllTasksFiltered_shouldReturnUserTasksForSpecificProject_whenNormalUserFiltersByValidProjectId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User userMakingRequest = normalUser; // normalUser faz parte do testProject 
        Long projectIdFilter = testProject.getId();

        when(projectRepository.findById(projectIdFilter)).thenReturn(Optional.of(testProject));
        doNothing().when(projectService).ensureUserCanViewProject(testProject, userMakingRequest);

        List<Task> tasksFromRepo = List.of(existingTask);
        Page<Task> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());

        when(taskRepository.findTasksFilteredByUser(null, null, projectIdFilter, userMakingRequest.getUserId(), pageable))
            .thenReturn(taskPageFromRepo);

        TaskResponseFilteredDTO filteredDto = new TaskResponseFilteredDTO();
        filteredDto.setId(existingTask.getId());
        filteredDto.setTitle(existingTask.getTitle());
        ProjectResponseFilteredDTO projectDto = new ProjectResponseFilteredDTO(); 
        projectDto.setId(projectIdFilter);
        projectDto.setTitle(testProject.getTitle());
        filteredDto.setProject(projectDto); 

        List<TaskResponseFilteredDTO> dtoList = List.of(filteredDto);
        PagedResponse<TaskResponseFilteredDTO> expectedPagedResponse = new PagedResponse<>(
            dtoList, taskPageFromRepo.getNumber(), taskPageFromRepo.getSize(),
            taskPageFromRepo.getTotalElements(), taskPageFromRepo.getTotalPages(), taskPageFromRepo.isLast()
        );
        when(pagedResponseMapper.toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class))
            .thenReturn(expectedPagedResponse);

        // Act
        PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
            null,          
            null,          
            projectIdFilter,
            pageable,
            userMakingRequest
        );

        // Assert
        assertNotNull(actualPagedResponse);
        assertFalse(actualPagedResponse.getContent().isEmpty());
        assertEquals(1, actualPagedResponse.getTotalElements());
        TaskResponseFilteredDTO taskInResponse = actualPagedResponse.getContent().get(0);
        assertEquals(existingTask.getTitle(), taskInResponse.getTitle());
        assertNotNull(taskInResponse.getProject());
        assertEquals(projectIdFilter, taskInResponse.getProject().getId()); 

        verify(projectRepository, times(1)).findById(projectIdFilter);
        verify(projectService, times(1)).ensureUserCanViewProject(testProject, userMakingRequest);
        verify(taskRepository, times(1)).findTasksFilteredByUser(null, null, projectIdFilter, userMakingRequest.getUserId(), pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class);
    }


    @Test
    @DisplayName("4.7: Should throw AccessDeniedException when normal user tries to filter tasks by project they do not belong to")
    void getAllTasksFiltered_shouldThrowAccessDeniedException_whenNormalUserFiltersByProjectIdTheyDoNotBelongTo() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User userMakingRequest = otherUser;
        Long projectIdFilter = testProject.getId(); 

        when(projectRepository.findById(projectIdFilter)).thenReturn(Optional.of(testProject));
        doThrow(new AccessDeniedException("You do not have permission to access this resource."))
            .when(projectService).ensureUserCanViewProject(testProject, userMakingRequest);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.getAllTasksFiltered(
                null,           
                null,           
                projectIdFilter, 
                pageable,
                userMakingRequest
            );
        });

        assertEquals("You do not have permission to access this resource.", exception.getMessage());

        verify(projectRepository, times(1)).findById(projectIdFilter);
        verify(projectService, times(1)).ensureUserCanViewProject(testProject, userMakingRequest); 
        verify(taskRepository, never()).findTasksFilteredByUser(any(), any(), any(), anyLong(), any(Pageable.class)); 
        verify(pagedResponseMapper, never()).toPagedResponse(any(), any());
    }

    @Test
    @DisplayName("4.9: Normal user should get their tasks filtered by status using getAllTasksFiltered")
    void getAllTasksFiltered_shouldReturnUserTasksFilteredByStatus_whenNormalUserFiltersByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User userMakingRequest = normalUser;
        String statusFilterString = "INPROGRESS"; 
        Status expectedStatusEnum = Status.INPROGRESS;

        List<Task> tasksFromRepo = List.of(existingTask); 
        Page<Task> taskPageFromRepo = new PageImpl<>(tasksFromRepo, pageable, tasksFromRepo.size());

        when(taskRepository.findTasksFilteredByUser(expectedStatusEnum, null, null, userMakingRequest.getUserId(), pageable)).thenReturn(taskPageFromRepo);

        TaskResponseFilteredDTO filteredDto = new TaskResponseFilteredDTO();
        filteredDto.setId(existingTask.getId());
        filteredDto.setTitle(existingTask.getTitle());
        filteredDto.setStatus(expectedStatusEnum); 
        
        List<TaskResponseFilteredDTO> dtoList = List.of(filteredDto);
        PagedResponse<TaskResponseFilteredDTO> expectedPagedResponse = new PagedResponse<>(
            dtoList, taskPageFromRepo.getNumber(), taskPageFromRepo.getSize(),
            taskPageFromRepo.getTotalElements(), taskPageFromRepo.getTotalPages(), taskPageFromRepo.isLast()
        );
        when(pagedResponseMapper.toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class))
            .thenReturn(expectedPagedResponse);

        // Act
        PagedResponse<TaskResponseFilteredDTO> actualPagedResponse = taskService.getAllTasksFiltered(
            statusFilterString, 
            null,               
            null,               
            pageable,
            userMakingRequest
        );

        // Assert
        assertNotNull(actualPagedResponse);
        assertFalse(actualPagedResponse.getContent().isEmpty());
        assertEquals(1, actualPagedResponse.getTotalElements());
        TaskResponseFilteredDTO taskInResponse = actualPagedResponse.getContent().get(0);
        assertEquals(existingTask.getTitle(), taskInResponse.getTitle());
        assertEquals(expectedStatusEnum, taskInResponse.getStatus()); 

        verify(projectRepository, never()).findById(anyLong()); // Nenhum filtro de projeto foi usado
        verify(projectService, never()).ensureUserCanViewProject(any(), any()); 
        verify(taskRepository, times(1)).findTasksFilteredByUser(expectedStatusEnum, null, null, userMakingRequest.getUserId(), pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(taskPageFromRepo, TaskResponseFilteredDTO.class);
    }

    @Test
    @DisplayName("5.2: Task owner should update task status successfully")
    void updateStatus_shouldUpdateStatus_whenUserIsOwner() {
        // Arrange
        TaskRequestPatchDTO patchDTO = new TaskRequestPatchDTO();
        patchDTO.setStatus(Optional.of(Status.TODO));
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();

        Task updatedTaskEntity = new Task();
        updatedTaskEntity.setId(taskId);
        updatedTaskEntity.setProject(testProject);
        updatedTaskEntity.setOwner(normalUser);
        updatedTaskEntity.setStatus(Status.TODO);
        updatedTaskEntity.setUsersResponsability(existingTask.getUsersResponsability());
        updatedTaskEntity.setTitle(existingTask.getTitle());


        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTaskEntity);

        TaskResponseDTO expectedResponse = new TaskResponseDTO();
        expectedResponse.setId(taskId);
        expectedResponse.setStatus(Status.TODO);
        when(modelMapper.map(updatedTaskEntity, TaskResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        TaskResponseDTO actualResponse = taskService.updateStatus(projectId, taskId, patchDTO, normalUser);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(Status.TODO, actualResponse.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("5.3: Responsible user should update task status successfully")
    void updateStatus_shouldUpdateStatus_whenUserIsResponsible() {
        // Arrange
        TaskRequestPatchDTO patchDTO = new TaskRequestPatchDTO();
        patchDTO.setStatus(Optional.of(Status.DONE));
        Long taskId = existingTask.getId(); 
        Long projectId = testProject.getId();

        Task updatedTaskEntity = new Task();
        updatedTaskEntity.setId(taskId);
        updatedTaskEntity.setProject(testProject);
        updatedTaskEntity.setOwner(normalUser);
        updatedTaskEntity.setStatus(Status.DONE);
        updatedTaskEntity.setUsersResponsability(existingTask.getUsersResponsability());
        updatedTaskEntity.setTitle(existingTask.getTitle());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTaskEntity);

        TaskResponseDTO expectedResponse = new TaskResponseDTO();
        expectedResponse.setId(taskId);
        expectedResponse.setStatus(Status.DONE);
        when(modelMapper.map(updatedTaskEntity, TaskResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        TaskResponseDTO actualResponse = taskService.updateStatus(projectId, taskId, patchDTO, responsibleUser);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(Status.DONE, actualResponse.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("5.4: Should throw AccessDeniedException when unauthorized user tries to update status")
    void updateStatus_shouldThrowAccessDeniedException_whenUserIsUnauthorized() {
        // Arrange
        TaskRequestPatchDTO patchDTO = new TaskRequestPatchDTO();
        patchDTO.setStatus(Optional.of(Status.DONE));
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.updateStatus(projectId, taskId, patchDTO, otherUser);
        });
        assertEquals(" FORBIDDEN - You do not have permission to modify this task.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("5.5: Should throw ResourceNotFoundException when task for status update is not found")
    void updateStatus_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        // Arrange
        TaskRequestPatchDTO patchDTO = new TaskRequestPatchDTO();
        patchDTO.setStatus(Optional.of(Status.DONE));
        Long nonExistentTaskId = 999L;
        Long projectId = testProject.getId();

        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateStatus(projectId, nonExistentTaskId, patchDTO, adminUser);
        });
        assertEquals("Task Not Found.", exception.getMessage());
    }

    @Test
    @DisplayName("5.6: Should throw ResourceNotFoundException if task does not belong to project during status update")
    void updateStatus_shouldThrowResourceNotFoundException_whenTaskDoesNotBelongToProject() {
        // Arrange
        TaskRequestPatchDTO patchDTO = new TaskRequestPatchDTO();
        patchDTO.setStatus(Optional.of(Status.DONE));
        Long taskId = existingTask.getId();
        Long differentProjectId = 777L; 

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateStatus(differentProjectId, taskId, patchDTO, adminUser);
        });
        assertEquals("Task does not belong to the specified project.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    @DisplayName("6.1: Admin should update task details successfully")
    void updateTask_shouldUpdateDetails_whenUserIsAdmin() {
        // Arrange
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Updated Title by Admin");
        requestDTO.setDescription("Updated Description");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(10));
        requestDTO.setUsersResponsability(List.of(responsibleUser.getUserId()));

        when(userRepository.findById(responsibleUser.getUserId())).thenReturn(Optional.of(responsibleUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        // aqui existingTask é o objeto que já existe no repositório e será atualizado, irei utilizar o doAnswer para simular o comportamento do ModelMapper e garante que quando modelMapper.map(requestDTO, task) é chamado no serviço, task (que é existingTask) no contexto d teste é realmente modificado.
        doAnswer(invocation -> {
            TaskRequestDTO sourceDto = invocation.getArgument(0);
            Task destinationTask = invocation.getArgument(1); //  será existingTask
            destinationTask.setTitle(sourceDto.getTitle());
            destinationTask.setDescription(sourceDto.getDescription());
            destinationTask.setDueDate(sourceDto.getDueDate());
            // usersResponsability é tratado separadamente no método de serviço após esta chamada de map
            return null; 
        }).when(modelMapper).map(eq(requestDTO), eq(existingTask)); 

        // Mock da operação de salvar. Agora ela pode retornar o mesmo objeto que foi modificado (existingTask).
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock do mapeamento final da entidade Task (agora atualizada) para TaskResponseDTO.
        // Este será chamado com 'existingTask' modificado (que se tornou 'updated' no serviço).
        when(modelMapper.map(any(Task.class), eq(TaskResponseDTO.class))).thenAnswer(invocation -> {
            Task taskInput = invocation.getArgument(0);
            TaskResponseDTO responseDto = new TaskResponseDTO();
            // Mapeie todos os campos relevantes do taskInput para o responseDto
            responseDto.setId(taskInput.getId());
            responseDto.setTitle(taskInput.getTitle()); // Deve ser o título atualizado
            responseDto.setDescription(taskInput.getDescription());
            responseDto.setDueDate(taskInput.getDueDate());
            responseDto.setStatus(taskInput.getStatus());
            return responseDto;
        });

        // Act
        TaskResponseDTO actualResponse = taskService.updateTask(projectId, taskId, requestDTO, adminUser);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(requestDTO.getTitle(), actualResponse.getTitle(), "O título na resposta do DTO deve ser o atualizado.");

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals(requestDTO.getTitle(), capturedTask.getTitle(), "O título da tarefa capturada para salvar deve ser o atualizado.");
        assertEquals(requestDTO.getDescription(), capturedTask.getDescription());
        assertEquals(requestDTO.getDueDate(), capturedTask.getDueDate());
        assertTrue(capturedTask.getUsersResponsability().stream().anyMatch(u -> u.getUserId().equals(responsibleUser.getUserId())), "A lista de responsabilidade deve conter o usuário esperado.");
        assertEquals(existingTask.getOwner().getUserId(), capturedTask.getOwner().getUserId(), "O proprietário da tarefa deve ser preservado."); 
        assertEquals(existingTask.getProject().getId(), capturedTask.getProject().getId(), "O projeto da tarefa deve ser preservado."); 
    }

    @Test
    @DisplayName("6.2: Task owner should update task details successfully")
    void updateTask_shouldUpdateDetails_whenUserIsOwner() {
        // Arrange
        Long taskId = existingTask.getId(); 
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Updated Title by Owner");
        requestDTO.setDescription("New description by owner");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(12)); 
        requestDTO.setUsersResponsability(List.of(normalUser.getUserId())); 

        when(userRepository.findById(normalUser.getUserId())).thenReturn(Optional.of(normalUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doAnswer(invocation -> {
            TaskRequestDTO sourceDto = invocation.getArgument(0);
            Task destinationTask = invocation.getArgument(1); // Este será 'existingTask'
            destinationTask.setTitle(sourceDto.getTitle());
            destinationTask.setDescription(sourceDto.getDescription());
            destinationTask.setDueDate(sourceDto.getDueDate());
            // usersResponsability é tratado separadamente no método de serviço.
            return null; 
        }).when(modelMapper).map(eq(requestDTO), eq(existingTask));

        Task savedTaskEntity = new Task();
        savedTaskEntity.setId(taskId);
        savedTaskEntity.setProject(existingTask.getProject()); // Preservar projeto
        savedTaskEntity.setOwner(existingTask.getOwner());   // Preservar owner
        savedTaskEntity.setStatus(existingTask.getStatus()); // Preservar status, pois não está no TaskRequestDTO
        savedTaskEntity.setTitle(requestDTO.getTitle());      // Título atualizado
        savedTaskEntity.setDescription(requestDTO.getDescription()); // Descrição atualizada
        savedTaskEntity.setDueDate(requestDTO.getDueDate());       // Data atualizada
        savedTaskEntity.setUsersResponsability(List.of(normalUser)); // Responsabilidade atualizada

        when(taskRepository.save(any(Task.class))).thenReturn(savedTaskEntity);

        TaskResponseDTO expectedResponse = new TaskResponseDTO();
        expectedResponse.setId(taskId);
        expectedResponse.setTitle(requestDTO.getTitle());
        expectedResponse.setDescription(requestDTO.getDescription());
        expectedResponse.setDueDate(requestDTO.getDueDate());

        when(modelMapper.map(savedTaskEntity, TaskResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        TaskResponseDTO actualResponse = taskService.updateTask(projectId, taskId, requestDTO, normalUser);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(requestDTO.getTitle(), actualResponse.getTitle());
        assertEquals(requestDTO.getDescription(), actualResponse.getDescription());
        assertEquals(requestDTO.getDueDate(), actualResponse.getDueDate());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals(requestDTO.getTitle(), capturedTask.getTitle());
        assertEquals(requestDTO.getDescription(), capturedTask.getDescription());
        assertEquals(requestDTO.getDueDate(), capturedTask.getDueDate());
        assertTrue(capturedTask.getUsersResponsability().stream().anyMatch(u -> u.getUserId().equals(normalUser.getUserId())));
    }


    @Test
    @DisplayName("6.3: Responsible user should update task details successfully")
    void updateTask_shouldUpdateDetails_whenUserIsResponsible() {
        // Arrange
        Long taskId = existingTask.getId(); 
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Updated Title by Responsible");
        requestDTO.setDescription("Description updated by responsible user");
        requestDTO.setDueDate(LocalDateTime.now().plusDays(15));
        requestDTO.setUsersResponsability(List.of(responsibleUser.getUserId()));

        when(userRepository.findById(responsibleUser.getUserId())).thenReturn(Optional.of(responsibleUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        doAnswer(invocation -> {
            TaskRequestDTO sourceDto = invocation.getArgument(0);
            Task destinationTask = invocation.getArgument(1); 
            destinationTask.setTitle(sourceDto.getTitle());
            destinationTask.setDescription(sourceDto.getDescription());
            destinationTask.setDueDate(sourceDto.getDueDate());
            // usersResponsability é tratado separadamente no método de serviço
            return null; 
        }).when(modelMapper).map(eq(requestDTO), eq(existingTask));

        //'savedTaskEntity' deve refletir o estado da tarefa após todas as modificações
        Task savedTaskEntity = new Task();
        savedTaskEntity.setId(taskId);
        savedTaskEntity.setProject(existingTask.getProject());
        savedTaskEntity.setOwner(existingTask.getOwner());
        savedTaskEntity.setStatus(existingTask.getStatus()); 
        savedTaskEntity.setTitle(requestDTO.getTitle());     
        savedTaskEntity.setDescription(requestDTO.getDescription()); 
        savedTaskEntity.setDueDate(requestDTO.getDueDate());       
        savedTaskEntity.setUsersResponsability(List.of(responsibleUser)); 

        when(taskRepository.save(any(Task.class))).thenReturn(savedTaskEntity);

        TaskResponseDTO expectedResponse = new TaskResponseDTO();
        expectedResponse.setId(taskId);
        expectedResponse.setTitle(requestDTO.getTitle());
        expectedResponse.setDescription(requestDTO.getDescription());
        expectedResponse.setDueDate(requestDTO.getDueDate());

        when(modelMapper.map(savedTaskEntity, TaskResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        TaskResponseDTO actualResponse = taskService.updateTask(projectId, taskId, requestDTO, responsibleUser);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(requestDTO.getTitle(), actualResponse.getTitle());
        assertEquals(requestDTO.getDescription(), actualResponse.getDescription()); 
        assertEquals(requestDTO.getDueDate(), actualResponse.getDueDate());      

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals(requestDTO.getTitle(), capturedTask.getTitle());
        assertEquals(requestDTO.getDescription(), capturedTask.getDescription());
        assertEquals(requestDTO.getDueDate(), capturedTask.getDueDate());
        assertTrue(capturedTask.getUsersResponsability().stream().anyMatch(u -> u.getUserId().equals(responsibleUser.getUserId())));
        assertEquals(existingTask.getOwner().getUserId(), capturedTask.getOwner().getUserId());
        assertEquals(existingTask.getProject().getId(), capturedTask.getProject().getId());
        assertEquals(existingTask.getStatus(), capturedTask.getStatus()); 
    }



    @Test
    @DisplayName("6.4: Should throw AccessDeniedException when unauthorized user tries to update task")
    void updateTask_shouldThrowAccessDeniedException_whenUserIsUnauthorized() {
        // Arrange
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO(); 
        requestDTO.setTitle("Attempted Update");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.updateTask(projectId, taskId, requestDTO, otherUser);
        });
        assertEquals(" FORBIDDEN - You do not have permission to modify this task.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("6.5: Should throw ResourceNotFoundException when task to update is not found")
    void updateTask_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        // Arrange
        Long nonExistentTaskId = 999L;
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO();

        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(projectId, nonExistentTaskId, requestDTO, adminUser);
        });
        assertEquals("Task Not Found.", exception.getMessage());
    }

    @Test
    @DisplayName("6.6: Should throw ResourceNotFoundException if task does not belong to project during update")
    void updateTask_shouldThrowResourceNotFoundException_whenTaskDoesNotBelongToProject() {
        // Arrange
        Long taskId = existingTask.getId();
        Long differentProjectId = 777L;
        TaskRequestDTO requestDTO = new TaskRequestDTO();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(differentProjectId, taskId, requestDTO, adminUser);
        });
        assertEquals("Task does not belong to the specified project.", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("6.7: Should throw ResourceNotFoundException if a user in usersResponsability from DTO is not found during update")
    void updateTask_shouldThrowResourceNotFoundException_whenResponsibleUserInDTONotFound() {
        // Arrange
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Update with invalid user");
        Long nonExistentUserId = 888L;
        requestDTO.setUsersResponsability(List.of(nonExistentUserId)); 

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty()); 

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(projectId, taskId, requestDTO, adminUser);
        });
        assertEquals("User Not Found.", exception.getMessage()); 
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("7.1: Admin should delete any task successfully")
    void deleteTask_shouldDeleteTask_whenUserIsAdmin() {
        // Arrange
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        // Act
        taskService.deleteTask(projectId, taskId, adminUser);

        // Assert
        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("7.2: Task owner should delete their own task successfully")
    void deleteTask_shouldDeleteTask_whenUserIsOwner() {
        // Arrange
        Long taskId = existingTask.getId(); // o dono é o normalUser
        Long projectId = testProject.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        // Act
        taskService.deleteTask(projectId, taskId, normalUser);

        // Assert
        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("7.3: Responsible user should delete task successfully")
    void deleteTask_shouldDeleteTask_whenUserIsResponsible() {
        // Arrange
        Long taskId = existingTask.getId(); // o responsável é o responsibleUser
        Long projectId = testProject.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).delete(existingTask);

        // Act
        taskService.deleteTask(projectId, taskId, responsibleUser);

        // Assert
        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("7.4: Should throw AccessDeniedException when unauthorized user tries to delete task")
    void deleteTask_shouldThrowAccessDeniedException_whenUserIsUnauthorized() {
        // Arrange
        Long taskId = existingTask.getId();
        Long projectId = testProject.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.deleteTask(projectId, taskId, otherUser);
        });
        assertEquals(" FORBIDDEN - You do not have permission to modify this task.", exception.getMessage());
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("7.5: Should throw ResourceNotFoundException when task to delete is not found")
    void deleteTask_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        // Arrange
        Long nonExistentTaskId = 999L;
        Long projectId = testProject.getId();

        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(projectId, nonExistentTaskId, adminUser);
        });
        assertEquals("Task Not Found.", exception.getMessage());
    }

    @Test
    @DisplayName("7.6: Should throw ResourceNotFoundException if task does not belong to project during deletion")
    void deleteTask_shouldThrowResourceNotFoundException_whenTaskDoesNotBelongToProject() {
        // Arrange
        Long taskId = existingTask.getId();
        Long differentProjectId = 777L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(differentProjectId, taskId, adminUser);
        });
        assertEquals("Task does not belong to the specified project.", exception.getMessage());
        verify(taskRepository, never()).delete(any(Task.class));
    }

}




