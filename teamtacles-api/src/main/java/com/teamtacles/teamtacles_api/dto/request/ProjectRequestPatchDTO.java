package com.teamtacles.teamtacles_api.dto.request;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequestPatchDTO {
    @Schema(description = "The new title for the project. If provided, the existing title will be updated.", example = "Updated Project Title")
    private Optional<String> title = Optional.empty();

    @Schema(description = "The new description for the project. If provided, the existing description will be updated.", example = "This is an updated description for the project.")
    private Optional<String> description = Optional.empty();

    @Schema(description = "A new list of user IDs (Long) to replace the current project team. If provided, the existing team will be fully replaced.", example = "[4, 5, 6]", type = "array")
    private Optional<List<Long>> team = Optional.empty();
}
