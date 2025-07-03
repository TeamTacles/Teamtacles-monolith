package com.teamtacles.teamtacles_api.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.ProjectRequestPatchDTO;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Task;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.util.TestDataAux;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestDataAux testDataAux;

    @BeforeEach
    void setUpEnvironment() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
        testDataAux.setUpTestUsers();
    }

    @Test
    @DisplayName("Should create a project and return 201 CREATED")
    void testCreateProject_whenUser_ShouldReturn201() throws Exception {
        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("API Project");
        dto.setDescription("Team task management API");
        dto.setTeam(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(post("/api/project") 
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("API Project"));

        var projects = projectRepository.findAll();
        assertFalse(projects.isEmpty(), "Project list should not be empty after creation");
    }

    @Test
    @DisplayName("Should not create a project when idUser from team not found and return 404")
    void testCreateProject_whenTeamIdNotFound_ShouldReturn404() throws Exception {
        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("API Project");
        dto.setDescription("Team task management API");
        dto.setTeam(List.of(10L)); // usuário não existente

        mockMvc.perform(post("/api/project") 
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not create a project when Title is blank and return 400")
    void testCreateProject_WithoutTitle_ShouldReturn400() throws Exception {
        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("");
        dto.setDescription("Team task management API");
        dto.setTeam(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(post("/api/project") 
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create a project when team is empty and return 400")
    void testCreateProject_WithoutTeam_ShouldReturn400() throws Exception {
        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("API Project");
        dto.setDescription("Team task management API");

        mockMvc.perform(post("/api/project") 
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all projects as ADMIN")
    void testGetAllProject_WhenAdmin_ShouldReturn200() throws Exception {

        Project savedProject = createUserOwnerProject();

        mockMvc.perform(get("/api/project/all") 
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("API Project User"));
    }

    @Test
    @DisplayName("Should return all projects when the user is in the team")
    void testGetAllProject_WhenUser_ShouldReturn200() throws Exception {
    
        Project savedProject = createAdminOwnerProject();

        mockMvc.perform(get("/api/project/all") 
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Should return any project by id when admin")
    void testGetProjectById_WhenAdmin_ShouldReturn200() throws Exception {
    
        Project savedProject = createUserOwnerProject();

        mockMvc.perform(get("/api/project/{project_id}", savedProject.getId()) 
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("API Project User"));
    }

    @Test
    @DisplayName("Shouldn't return any project by id when id not exists")
    void testGetProjectById_WhenIdNotExists_ShouldReturn404() throws Exception {
    
        mockMvc.perform(get("/api/project/5") //projeto com esse id não existe
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not return any project by ID when the user is not in the team")
    void testGetProjectById_WhenUserNotInTeam_ShouldReturn403() throws Exception {
    
        Project savedProject = createAdminOwnerProject(); // projeto exclusivo do admin

        mockMvc.perform(get("/api/project/{project_id}", savedProject.getId()) 
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should partially update project as Admin with 200 OK")
    void testPartialUpdate_WhenAdmin_ShouldReturn200() throws Exception {

        Project savedProject = createUserOwnerProject();

        ProjectRequestPatchDTO dto = new ProjectRequestPatchDTO();
        dto.setTitle(Optional.of("Backend project"));

        mockMvc.perform(patch("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Backend project"));
    }

    @Test
    @DisplayName("Should partially update project as User - Owner with 200 OK")
    void testPartialUpdate_WhenUserOwner_ShouldReturn200() throws Exception {

        Project savedProject = createUserOwnerProject();

        ProjectRequestPatchDTO dto = new ProjectRequestPatchDTO();
        dto.setTitle(Optional.of("Backend project"));

        mockMvc.perform(patch("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Backend project"));
    }

    @Test
    @DisplayName("Should not partially update project with User in team with 403 FORBIDDEN")
    void testPartialUpdate_WhenUserInTeam_ShouldReturn403() throws Exception {
        
        Project savedProject = createAdminOwnerProjectAndUserTeam();

        ProjectRequestPatchDTO dto = new ProjectRequestPatchDTO();
        dto.setTitle(Optional.of("Backend project"));

        mockMvc.perform(patch("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should not partially update project when User not in the team with 403 FORBIDDEN")
    void testPartialUpdate_WhenUser_ShouldReturn403() throws Exception {
        
        Project savedProject = createAdminOwnerProject();

        ProjectRequestPatchDTO dto = new ProjectRequestPatchDTO();
        dto.setTitle(Optional.of("Backend project"));

        mockMvc.perform(patch("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fully update project as Admin with 200 OK")
    void testFullUpdate_WhenAdmin_ShouldReturn200() throws Exception {

        Project savedProject = createUserOwnerProject();

        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("Backend Project");
        dto.setDescription("With Java");
        dto.setTeam(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Backend Project"))
            .andExpect(jsonPath("$.description").value("With Java"));
    }

    @Test
    @DisplayName("Should fully update project as User is Owner with 200 OK")
    void testFullUpdate_WhenUserOwner_ShouldReturn200() throws Exception {

        Project savedProject = createUserOwnerProject();

        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("Backend Project");
        dto.setDescription("With Java");
        dto.setTeam(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Backend Project"))
            .andExpect(jsonPath("$.description").value("With Java"));
    }

    @Test
    @DisplayName("Should not fully update project when User not creator with 403 FORBIDDEN")
    void testFullUpdate_WhenUser_ShouldReturn403() throws Exception {
        Project savedProject = createAdminOwnerProjectAndUserTeam();

        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setTitle("Backend Project");
        dto.setDescription("With Java");
        dto.setTeam(List.of(testDataAux.getNormalUser().getUserId()));

        mockMvc.perform(put("/api/project/{project_id}", savedProject.getId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete project as Admin with 204 NO CONTENT")
    void testDeleteTask_WhenAdmin_ShouldReturn204() throws Exception {
        Project savedProject = createUserOwnerProject();

        mockMvc.perform(delete("/api/project/{project_id}", savedProject.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should delete project as user owner with 204 NO CONTENT")
    void testDeleteTask_WhenUserOwner_ShouldReturn204() throws Exception {
        Project savedProject = createUserOwnerProject();

        mockMvc.perform(delete("/api/project/{project_id}", savedProject.getId())
                .header("Authorization", "Bearer " + testDataAux.getAdminUserToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should not delete project when not the creator with 403 FORBIDDEN")
    void testDeleteTask_WhenNotUserCreator_ShouldReturn403() throws Exception {
        Project savedProject = createAdminOwnerProjectAndUserTeam();

        mockMvc.perform(delete("/api/project/{project_id}", savedProject.getId())
                .header("Authorization", "Bearer " + testDataAux.getNormalUserToken()))
                .andExpect(status().isForbidden());
    }

    private Project createUserOwnerProject(){
        Project project = new Project();
        project.setTitle("API Project User");
        project.setDescription("Team task management API");
        project.setTeam(List.of(testDataAux.getNormalUser())); 
        project.setCreator(testDataAux.getNormalUser());

        return projectRepository.save(project);
    }

    private Project createAdminOwnerProjectAndUserTeam() {
        Project project = new Project();
        project.setTitle("API Project");
        project.setDescription("Team task management API");
        project.setTeam(List.of(testDataAux.getNormalUser())); 
        project.setCreator(testDataAux.getAdminUser());

        return projectRepository.save(project);
    }


    private Project createAdminOwnerProject() {
        Project project = new Project();
        project.setTitle("API Project");
        project.setDescription("Team task management API");
        project.setTeam(List.of(testDataAux.getAdminUser())); 
        project.setCreator(testDataAux.getAdminUser());

        return projectRepository.save(project);
    }

}
