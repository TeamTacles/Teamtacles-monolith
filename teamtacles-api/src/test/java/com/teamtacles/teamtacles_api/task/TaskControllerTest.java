package com.teamtacles.teamtacles_api.task;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtacles.teamtacles_api.dto.request.TaskRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.TaskRequestPatchDTO;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Task;
import com.teamtacles.teamtacles_api.model.enums.Status;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.TaskRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.util.TestDataAux;
import com.teamtacles.teamtacles_api.util.TestDataProjectAux;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestDataAux testDataAux;

    @Autowired
    private TestDataProjectAux testDataProjectAux;

    @BeforeEach
    void setUpEnvironment() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        testDataAux.setUpTestUsers();
        testDataProjectAux.setUpTestProject();
    }

    @Test
    @DisplayName("Should create a task and return 201 CREATED")
    void testCreateTask_ShouldReturn201() throws Exception {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Review project documentation");
        dto.setDescription("Check if the API documentation is updated");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(post("/api/project/{project_id}/task", testDataProjectAux.getProject().getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Review project documentation"))
                .andExpect(jsonPath("$.status").value("TODO"));

        var tasks = taskRepository.findAll();
        assertFalse(tasks.isEmpty(), "Task list should not be empty after creation");
        assertEquals(testDataAux.getNormalUser().getUserId(), tasks.get(0).getOwner().getUserId(), "Task should be assigned to the correct user");   
    }

    @Test
    @DisplayName("Should throw exception when creating task in non-existent project return 404")
    void testCreateTask_WhenProjectDoesntExists_ShouldReturn404() throws Exception {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Review project documentation");
        dto.setDescription("Check if the API documentation is updated");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(post("/api/project/{project_id}/task", 10) //id do projeto não existe
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw exception when setting a team where user doesnt exists - return 404")
    void testCreateTask_WhenUserIdInTeamDoesntExists_ShouldReturn404() throws Exception {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Review project documentation");
        dto.setDescription("Check if the API documentation is updated");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(10L)); // usuário não existente no sistema

        mockMvc.perform(post("/api/project/{project_id}/task", testDataProjectAux.getProject().getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());  
    }

    @Test
    @DisplayName("Should return task by ID as Admin with 200 OK")
    void testGetTaskById_WhenAdmin_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Review project documentation"));
    }

    @Test
    @DisplayName("Should return task by ID when User Responsible/Creator with 200 OK")
    void testGetTaskById_WhenUser_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask(); // user is the creator and responsible

        mockMvc.perform(get("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Review project documentation"));
    }


    @Test
    @DisplayName("Should forbid access to task by ID when not responsible with 403 FORBIDDEN")
    void testGetTaskById_WhenUserNotResponsible_ShouldReturn403() throws Exception {
        Task savedTask = createAdminTask();

        mockMvc.perform(get("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should throw exception when task by ID doesnt exists 404 Not Found")
    void testGetTaskById_WhenIdDoesntExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), 5)
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should throw an exception when task by ID does not exist in the specified project 404 Not Found")
    void testGetTaskById_WhenIdDoesntExistsInProject_ShouldReturn404() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{id_project}/task/{id_task}", 10, savedTask.getId()) // projeto não existe
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return tasks by user ID as admin with 200 OK")
    void testGetTasksByUserId_WhenAdmin_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{project_id}/tasks/user/{user_id}?page=0&size=10", testDataProjectAux.getProject().getId(), testDataAux.getNormalUser().getUserId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Review project documentation"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("Should forbid user from getting others tasks with 403 FORBIDDEN")
    void testGetTasksByUserId_FromAnotherUser_ShouldReturn403() throws Exception {
        Task savedTask = createAdminTask();

        mockMvc.perform(get("/api/project/{project_id}/tasks/user/{user_id}", testDataProjectAux.getProject().getId(), testDataAux.getAdminUser().getUserId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())) // tentando acessar as tasks de outro usuario
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should list tasks under my responsability or creation")
    void testGetTasksByUserId_WhenUserResponsible_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{project_id}/tasks/user/{user_id}", testDataProjectAux.getProject().getId(), testDataAux.getNormalUser().getUserId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Review project documentation"));
    }

    @Test
    @DisplayName("Should throw exception when tries to search for tasks with invalid project ID with 404 OK")
    void testGetTasksByUserId_WhenInvalidProjectId_ShouldReturn404() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{project_id}/tasks/user/{user_id}?page=0&size=10", 14, testDataAux.getNormalUser().getUserId()) // id do projeto inválido
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw exception when tries to search for user with invalid user ID with 404 OK")
    void testGetTasksByUserId_WhenInvalidUserId_ShouldReturn404() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(get("/api/project/{project_id}/tasks/user/{user_id}", testDataProjectAux.getProject().getId(), 10)
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all tasks when no filters are applied")
    void testGetTaks_WhenAdmin_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();
        Task savedTask2 = createAdminTask();

        mockMvc.perform(get("/api/project/task/search")
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Review project documentation"))
                .andExpect(jsonPath("$.content[1].title").value("Admin's Task"));
    }

    @Test
    @DisplayName("Should return all tasks when no filters are applied")
    void testGetTaks_WhenUser_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();
        Task savedTask2 = createAdminTask();

        mockMvc.perform(get("/api/project/task/search")
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Review project documentation"))
                .andExpect(jsonPath("$.content[1].title").doesNotExist()); // não aparece para o usuario
    }

    @Test
    @DisplayName("Should return tasks filtered by status, dueDate and projectId when admin with 200 OK")
    void testGetTasksFiltered_WhenAdminWithFilters_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        String status = savedTask.getStatus().toString();
        String dueDate = (LocalDateTime.now().plusDays(3)).toString();
        Long projectId = savedTask.getProject().getId();

        mockMvc.perform(get("/api/project/task/search")
            .param("status", status)
            .param("dueDate", dueDate)
            .param("projectId", String.valueOf(projectId))
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].status").value(status))
            .andExpect(jsonPath("$.content[0].project.id").value(projectId))
            .andExpect(jsonPath("$.content[0].title").value("Review project documentation"));

            LocalDateTime dueDateTime = LocalDateTime.parse(dueDate);
            assertTrue(savedTask.getDueDate().isBefore(dueDateTime) || savedTask.getDueDate().isEqual(dueDateTime));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when normal user tries to filter by a project they don't belong to")
    void testGetTasksFiltered_WithUnauthorizedProjectId_ShouldReturn403() throws Exception {
        Project project = new Project();
        project.setTitle("API Project");
        project.setDescription("Team task management API");
        project.setTeam(List.of(testDataAux.getAdminUser()));  // only admin user on the team
        project.setCreator(testDataAux.getAdminUser());

        project = projectRepository.save(project);

        mockMvc.perform(get("/api/project/task/search")
            .param("projectId", String.valueOf(project.getId()))
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return tasks of normal user filtered by valid status")
    void testGetTasksFiltered_WithValidStatusForNormalUser_ShouldReturnTasks() throws Exception {
        Task savedTask = createUserTask();

        String status = savedTask.getStatus().toString();

        mockMvc.perform(get("/api/project/task/search")
            .param("status", status)
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.content[0].status").value(status));
    }

    @Test
    @DisplayName("Should return 404 Not Found when filtering tasks with non-existent projectId")
    void testGetTasksFiltered_WithNonExistentProjectId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/project/task/search")
            .param("projectId", "100")  // ID de projeto inexistente
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when filtering tasks with invalid status")
    void testGetTasksFiltered_WithInvalidStatus_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/project/task/search")
            .param("status", "LOADING")  // Status inválido
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should partially update task as Admin with 200 OK")
    void testPartialUpdate_WhenAdmin_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        TaskRequestPatchDTO dto = new TaskRequestPatchDTO();
        dto.setStatus(Optional.of(Status.INPROGRESS));

        mockMvc.perform(patch("/api/project/{id_project}/task/{id_task}/updateStatus", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INPROGRESS"));
    }

    @Test
    @DisplayName("Should partially update task as responsible User with 200 OK")
    void testPartialUpdate_WhenUser_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        TaskRequestPatchDTO dto = new TaskRequestPatchDTO();
        dto.setStatus(Optional.of(Status.INPROGRESS));

        mockMvc.perform(patch("/api/project/{id_project}/task/{id_task}/updateStatus", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INPROGRESS"));
    }

    @Test
    @DisplayName("Should forbid partial update of task by unauthorized User with 403 FORBIDDEN")
    void testPartialUpdate_WhenUserNotResponsible_ShouldReturn403() throws Exception {
        Task savedTask = createAdminTask();

        TaskRequestPatchDTO dto = new TaskRequestPatchDTO();
        dto.setStatus(Optional.of(Status.INPROGRESS));

        mockMvc.perform(patch("/api/project/{id_project}/task/{id_task}/updateStatus", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fully update task as Admin with 200 OK")
    void testFullUpdate_WhenAdmin_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Create README.md");
        dto.setDescription("Create the README.md file");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Create README.md"));
    }

    @Test
    @DisplayName("Should fully update task as owner User with 200 OK")
    void testFullUpdate_WhenUser_ShouldReturn200() throws Exception {
        Task savedTask = createUserTask();

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Create README.md");
        dto.setDescription("Create the README.md file");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Create README.md"));
    }

    @Test
    @DisplayName("Should forbid full update of Admin's task by User with 403 FORBIDDEN")
    void testFullUpdate_WhenUserNotResponsible_ShouldReturn403() throws Exception {
        Task savedTask = createAdminTask();

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Create README.md");
        dto.setDescription("Create the README.md file");
        dto.setDueDate(LocalDateTime.now().plusDays(5));
        dto.setUsersResponsability(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete task as Admin with 204 NO CONTENT")
    void testDeleteTask_WhenAdmin_ShouldReturn204() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(delete("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should delete task as owner User with 204 NO CONTENT")
    void testDeleteTask_WhenUser_ShouldReturn204() throws Exception {
        Task savedTask = createUserTask();

        mockMvc.perform(delete("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should forbid deletion of Admin's task by User with 403 FORBIDDEN")
    void testDeleteTask_WhenUserNotResponsible_ShouldReturn403() throws Exception {
        Task savedTask = createAdminTask();

        mockMvc.perform(delete("/api/project/{id_project}/task/{id_task}", testDataProjectAux.getProject().getId(), savedTask.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andExpect(status().isForbidden());
    }

    private Task createUserTask() {
        Task task = new Task();
        task.setTitle("Review project documentation");
        task.setDescription("Check if the API documentation is updated");
        task.setDueDate(LocalDateTime.now().plusDays(2));
        task.setUsersResponsability(List.of(testDataAux.getNormalUser()));
        task.setOwner(testDataAux.getNormalUser());
        task.setStatus(Status.TODO);
        task.setProject(testDataProjectAux.getProject());

        return taskRepository.save(task);
    }

    private Task createAdminTask() {
        Task adminTask = new Task();
        adminTask.setTitle("Admin's Task");
        adminTask.setDescription("Admin's task description");
        adminTask.setDueDate(LocalDateTime.now().plusDays(2));
        adminTask.setUsersResponsability(List.of(testDataAux.getAdminUser()));
        adminTask.setOwner(testDataAux.getAdminUser());
        adminTask.setStatus(Status.TODO);
        adminTask.setProject(testDataProjectAux.getProject());

        return taskRepository.save(adminTask);
    }
}
