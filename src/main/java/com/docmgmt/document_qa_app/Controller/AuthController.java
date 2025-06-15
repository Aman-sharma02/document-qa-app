package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.JwtRequestDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserRegistrationDTO;
import com.docmgmt.document_qa_app.Service.AuthService;
import com.docmgmt.document_qa_app.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication Controller", description = "Handles login and registration operations. These endpoints are publicly accessible and do not require authentication.")
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        super();
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(
            summary = "User login endpoint",
            description = "Authenticates a user with username and password. Returns a JWT token upon successful authentication that can be used for subsequent API calls.")
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@Valid @RequestBody @Parameter(
            description = "Login credentials containing username and password",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = JwtRequestDTO.class),
                    examples = @ExampleObject(
                            name = "Login Request",
                            value = """
                    {
                        "username": "john_doe",
                        "password": "securePassword123"
                    }
                    """
                    ))) JwtRequestDTO jwtRequestDTO) {
        try {
            return authService.authenticateUser(jwtRequestDTO);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Register new user account",
            description = "Creates a new user in DB. The user will be assigned a default role of VIEWER and can login immediately after successful registration.")
    @PostMapping(value = "/registration")
    public ResponseEntity<String> registerUserAccount(@Valid @RequestBody @Parameter(
            description = "User registration information including username and password",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UserRegistrationDTO.class),
                    examples = @ExampleObject(
                            name = "Registration Request",
                            value = """
                    {
                        "username": "john_doe",
                        "email": "john.doe@example.com",
                        "password": "securePassword123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """
                    ))) UserRegistrationDTO userRegistrationDTO) {
        try {
            userService.save(userRegistrationDTO);
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot register this user: " + e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("User registered successfully");
    }
}
