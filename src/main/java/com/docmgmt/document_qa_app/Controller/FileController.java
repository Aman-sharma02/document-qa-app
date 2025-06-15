package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.FileDTO;
import com.docmgmt.document_qa_app.Service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/file")
@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
@Tag(name = "File Management Controller", description = "Handles file operations including upload, update, and deletion. Requires ADMIN or EDITOR role for access.")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        super();
        this.fileService = fileService;
    }

    @Operation(summary = "Upload a new file", description = "Uploads a new file and stores in DB")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@Valid @ModelAttribute @Parameter(
            description = "File upload data including the file and metadata",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = FileDTO.class)
            ))FileDTO file) {
        try {
            if(file.getFile() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Cannot be empty");
            return new ResponseEntity<>(fileService.uploadFile(file), HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot Upload file: " + rs.getReason(),rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot Upload file: " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update an existing file", description = "Updates an existing file in the DB by replacing it with a new version. The file ID must exist in the system")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateFile(@Valid @ModelAttribute @Parameter(
            description = "Updated file data including the new file and metadata",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = FileDTO.class)
            )) FileDTO file, @PathVariable @Parameter(
            description = "Unique identifier of the file to update",
            required = true,
            example = "123L"
    ) Long id) {
        try {
            if(file.getFile() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Cannot be empty");
            return new ResponseEntity<>(fileService.updateFile(file, id), HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot Update file: " + rs.getReason(),rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot Update file: " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete a file", description = "Permanently deletes a file from DB. This action cannot be undone")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable @Parameter(
            description = "Unique identifier of the file to delete",
            required = true,
            example = "123L") Long id) {
        try {
            return new ResponseEntity<>(fileService.deleteFile(id), HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot Delete file: " + rs.getReason(),rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot Delete file: " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}