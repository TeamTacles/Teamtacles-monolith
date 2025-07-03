package com.teamtacles.teamtacles_api.dto.request;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teamtacles.teamtacles_api.model.enums.ERole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequestDTO {

    @Schema(description = "The new role to assign to the user. Must be one of the allowed values.", example = "ADMIN", required = true)
    @NotNull(message = "Role must be ADMIN, USER")
    private String role;
}