package com.docmgmt.document_qa_app.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FileDTO {

    private MultipartFile file;

    private String description;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Keywords are required")
    @Size(min = 3, max = 200, message = "Keywords must be between 3 and 200 characters")
    private String keyword;

    private String content;
}
