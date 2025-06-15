package com.docmgmt.document_qa_app.Controller;
import com.docmgmt.document_qa_app.Model.FileEntity;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import com.docmgmt.document_qa_app.Service.FileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/fileviewer")
@Tag(name = "File Viewer Controller", description = "APIs for viewing and retrieving file and file metadata. Requires ADMIN, EDITOR or VIEWER role for access.")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('VIEWER')")
public class ViewController {

    private final FileService fileService;

    public ViewController(FileService fileService) {
        super();
        this.fileService = fileService;
    }

    @Operation(summary = "Retrieve file by FileId", description = "Retrieves a file by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<?> searchFileById(@PathVariable @Parameter(description = "Unique identifier of the file", required = true, example = "123L") Long id) {
        try {
            FileEntity file = fileService.findById(id);
            HttpHeaders headers = new HttpHeaders();
            try {
                headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            } catch (Exception e) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Fallback for unknown content types
            }
            headers.setContentDispositionFormData("attachment",
                    URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20"));
            headers.setContentLength(file.getData().length);
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            headers.setPragma("no-cache");
            headers.setExpires(0);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getData());
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch file: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get file metadata by FileId", description = "Retrieves metadata information for a file including the actual file content.")
    @GetMapping("/meta/{id}")
    public ResponseEntity<?> searchFileMetadataById(@PathVariable @Parameter(description = "Unique identifier of the file", required = true, example = "123L") Long id) {
        try {
            return new ResponseEntity<>(fileService.findFileMetaById(id),HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get files by editor ID", description = "Retrieves paginated file metadata for files associated with a specific editor.")
    @GetMapping("/editor/{editorId}")
    public ResponseEntity<?> searchFileByEditorId(
            @PathVariable @Parameter(description = "Unique identifier of the editor", required = true, example = "456L") Long editorId,
            @RequestParam(defaultValue = "0") @Parameter(
                    description = "Page number for pagination (0-based indexing)",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", maximum = "1000")
            ) int page,
            @RequestParam(defaultValue = "10") @Parameter(
                    description = "Number of files to return per page",
                    example = "10",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100")
            ) int size,
            @RequestParam(defaultValue = "id") @Parameter(
                    description = "Field to sort results by",
                    example = "id",
                    schema = @Schema(
                            type = "string"
                    )
            ) String sortBy,
            @RequestParam(defaultValue = "ASC") @Parameter(
                    description = "Sort direction - ASC for ascending, DESC for descending.",
                    example = "DESC",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ASC", "DESC"}
                    )
            ) String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        try {
            return new ResponseEntity<>(fileService.findFileMetaByEditorId(editorId, pageable),HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Search files by file type", description = "Retrieves paginated file metadata filtered by file type.")
    @GetMapping("/fileType")
    public ResponseEntity<?> searchFileByType(
            @RequestBody @Parameter(description = "File type to filter by", required = true, example = "pdf") String fileType,
            @RequestParam(defaultValue = "0") @Parameter(
                    description = "Page number for pagination (0-based indexing)",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", maximum = "1000")
            ) int page,
            @RequestParam(defaultValue = "10") @Parameter(
                    description = "Number of files to return per page",
                    example = "10",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100")
            ) int size,
            @RequestParam(defaultValue = "id") @Parameter(
                    description = "Field to sort results by",
                    example = "id",
                    schema = @Schema(
                            type = "string"
                    )
            ) String sortBy,
            @RequestParam(defaultValue = "ASC") @Parameter(
                    description = "Sort direction - ASC for ascending, DESC for descending.",
                    example = "DESC",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ASC", "DESC"}
                    )
            ) String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        try {
            return new ResponseEntity<>(fileService.findFileMetaByFileType(fileType, pageable), HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Search files by keyword", description = "Retrieves paginated file metadata filtered by keyword search.")
    @GetMapping("/keyword")
    public ResponseEntity<?> searchFileByKeyword(
            @RequestBody @Parameter(description = "Keyword to search for in file metadata", required = true, example = "important document") String keyword,
            @RequestParam(defaultValue = "0") @Parameter(
                    description = "Page number for pagination (0-based indexing)",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", maximum = "1000")
            ) int page,
            @RequestParam(defaultValue = "10") @Parameter(
                    description = "Number of files to return per page",
                    example = "10",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100")
            ) int size,
            @RequestParam(defaultValue = "id") @Parameter(
                    description = "Field to sort results by",
                    example = "id",
                    schema = @Schema(
                            type = "string"
                    )
            ) String sortBy,
            @RequestParam(defaultValue = "ASC") @Parameter(
                    description = "Sort direction - ASC for ascending, DESC for descending.",
                    example = "DESC",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"ASC", "DESC"}
                    )
            ) String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        try {
            return new ResponseEntity<>(fileService.findFileMetaByKeyword(keyword, pageable),HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch file metadata: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
