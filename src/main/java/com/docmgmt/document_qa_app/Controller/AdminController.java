package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.UpdateRolesDTO;
import com.docmgmt.document_qa_app.Service.FileService;
import com.docmgmt.document_qa_app.Service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Controller", description = "Admin APIs for Cache and User management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    private final FileService fileService;

    public AdminController(UserService userService, FileService fileService) {
        super();
        this.userService = userService;
        this.fileService = fileService;
    }

    @Operation(
            summary = "Update user role by username",
            description = "Updates the role of a specific user identified by username. Only Admin can perform this operation.")
    @PutMapping("/role")
    public ResponseEntity<String> updateUserRole(@Valid @RequestBody @Parameter(
            description = "User role update information containing username and new role",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateRolesDTO.class))
    ) UpdateRolesDTO updateRolesDTO) {
        try {
            return userService.updateRole(updateRolesDTO);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot Update role: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot Update role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Delete user by UserId",
            description = "Permanently deletes a user from the DB. This operation cannot be undone")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Parameter(
            description = "Unique identifier of the user to delete",
            required = true,
            example = "123L"
    ) Long id) {
        try {
            return userService.deleteUser(id);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot Delete user: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot Delete user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the DB with their basic information")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch users: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch users: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Get user by UserId",
            description = "Retrieves user details using their unique ID.")
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable @Parameter(
            description = "Unique identifier of the user to retrieve",
            required = true,
            example = "123L") Long id ) {
        try {
            return userService.getUserById(id);
        } catch (ResponseStatusException rs) {
            return new ResponseEntity<>("Cannot fetch user: " + rs.getReason(), rs.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot fetch user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Get cache statistics",
            description = "Retrieves number of keys stored in cache")
    @GetMapping("/CacheStats")
    public ResponseEntity<String> getCacheStats() {
        try {
            return fileService.getCacheStats();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch cache stats: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Clear system cache",
            description = "Clears all cached data from the system")
    @GetMapping("/clearCache")
    public ResponseEntity<String> clearCache() {
        try {
            return new ResponseEntity<>(fileService.clearCache(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to clear cache: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}