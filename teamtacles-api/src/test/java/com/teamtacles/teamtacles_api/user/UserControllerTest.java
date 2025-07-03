package com.teamtacles.teamtacles_api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtacles.teamtacles_api.dto.request.RoleRequestDTO;
import com.teamtacles.teamtacles_api.dto.request.UserRequestDTO;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.repository.ProjectRepository;
import com.teamtacles.teamtacles_api.repository.TaskRepository;
import com.teamtacles.teamtacles_api.repository.UserRepository;
import com.teamtacles.teamtacles_api.util.TestDataAux;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository; // Injetar ProjectRepository

    @Autowired
    private TaskRepository taskRepository;     // Injetar TaskRepository

    @Autowired
    private TestDataAux testDataAux;

    @BeforeEach
    void setUpEnvironment() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        testDataAux.setUpTestUsers();
    }

    @Test
    @DisplayName("Should Register an User")
    void testRegisterAnUser_ShouldReturn201() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName("testUsername");
        dto.setEmail("testUsername@example.com");
        dto.setPassword("12345");
        dto.setPasswordConfirm("12345");

        mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(dto)))
		 	.andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userName").value("testUsername"))
            .andExpect(jsonPath("$.email").value("testUsername@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist()); 
    }

    @Test
    @DisplayName("Should return 409 when username already exists")
    void createUser_whenUsernameExists_shouldReturn409() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName(testDataAux.getNormalUser().getUserName());
        dto.setEmail("newemail@example.com");
        dto.setPassword("password123");
        dto.setPasswordConfirm("password123");

        mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict()); 
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void createUser_whenEmailExists_shouldReturn409() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName("newusername");
        dto.setEmail(testDataAux.getNormalUser().getEmail()); 
        dto.setPassword("password123");
        dto.setPasswordConfirm("password123");

        mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict()); 
    }

    @Test
    @DisplayName("Should return 400 when passwords do not match")
    void createUser_whenPasswordsDoNotMatch_shouldReturn400() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUserName("uniqueusername");
        dto.setEmail("uniqueemail@example.com");
        dto.setPassword("password123");
        dto.setPasswordConfirm("differentPassword");

        mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest()); 
    }

    @Test
    @DisplayName("Should update user role when admin")
    void testExchangeRoleUser_WhenAdmin_ShouldReturn200() throws Exception {
        User user = testDataAux.getNormalUser();

        RoleRequestDTO dto = new RoleRequestDTO();
        dto.setRole("ADMIN");

        mockMvc.perform(patch("/api/user/{id_user}/exchangepaper", user.getUserId())
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
            .contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[0].roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("Should not update user role when user")
    void testExchangeRoleUser_WhenUser_ShouldReturn403() throws Exception {
        User user = testDataAux.getNormalUser();

        RoleRequestDTO dto = new RoleRequestDTO();
        dto.setRole("ADMIN");


         mockMvc.perform(patch("/api/user/{id_user}/exchangepaper", user.getUserId())
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should throw exception if role is not valid")
    void testExchangeRoleUser_WhenAdmin_ShouldReturn400() throws Exception {
        User user = testDataAux.getNormalUser();

        RoleRequestDTO dto = new RoleRequestDTO();
        dto.setRole("LEADER"); // inválido


         mockMvc.perform(patch("/api/user/{id_user}/exchangepaper", user.getUserId())
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
            .contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return paged list of all users")
    void testGetAllUsers_WhenAdmin_ShouldReturnPagedUsers() throws Exception {
        User user = testDataAux.getNormalUser();

        mockMvc.perform(get("/api/user")
            .header("Authorization", "Bearer " + testDataAux.getAdminUserToken())
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    @DisplayName("Should forbid paged list of all users when not admin")
    void testGetAllUsers_WhenUser_ShouldReturn403() throws Exception {
         User user = testDataAux.getNormalUser();

        mockMvc.perform(get("/api/user")
            .header("Authorization", "Bearer " + testDataAux.getNormalUserToken())
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to list all users")
    void testGetAllUsers_WhenUnauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/user")
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isUnauthorized());
    }
}
