package com.docmgmt.document_qa_app.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRolesDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|EDITOR|VIEWER)$",
            message = "Role must be one of: ADMIN, EDITOR, VIEWER")
    private String role;
}
