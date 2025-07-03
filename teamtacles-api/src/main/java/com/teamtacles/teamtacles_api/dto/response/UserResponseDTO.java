package com.teamtacles.teamtacles_api.dto.response;

import java.util.List;
import java.util.Set;

import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.Role;
import com.teamtacles.teamtacles_api.model.Task;
import com.teamtacles.teamtacles_api.model.enums.ERole;

import io.swagger.v3.oas.annotations.media.Schema;

import com.teamtacles.teamtacles_api.dto.response.RoleResponseDTO; 


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO{
    @Schema(description = "The unique username of the user.", example = "jane.doe")
    private String userName;

    @Schema(description = "The unique email address of the user.", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "A set of roles assigned to the user.", type = "array")
    private Set<RoleResponseDTO> roles;
}