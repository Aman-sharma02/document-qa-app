package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Service.FileService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/qa")
@Tag(name = "Question & Answer Controller", description = "Handles question and answer operation. Requires ADMIN, EDITOR or VIEWER role for access.")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('VIEWER')")
public class QAController {

    private final FileService fileService;

    public QAController(FileService fileService) {
        super();
        this.fileService = fileService;
    }

    @Operation(summary = "Ask a question and get relevant documents metadata", description = "Retrieves all the relevant documents as per the question")
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody @Parameter(
            description = "Questions should be clear and specific for better results.",
            required = true,
            schema = @Schema(
                    type = "string",
                    minLength = 3,
                    maxLength = 500,
                    example = "What is the company's remote work policy?"
            )) String question,
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
            return new ResponseEntity<>(fileService.answerQuestion(question, pageable), HttpStatus.OK);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot answer question: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot answer question: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}