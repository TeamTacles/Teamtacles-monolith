package com.teamtacles.teamtacles_api.dto.response;

import com.teamtacles.teamtacles_api.model.enums.ERole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoleResponseDTO {
    @Schema(description = "The name of the user's role.", example = "USER")
    private ERole roleName;
}
