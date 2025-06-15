package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.JwtRequestDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserRegistrationDTO;
import com.docmgmt.document_qa_app.Model.JwtResponse;
import com.docmgmt.document_qa_app.Service.AuthService;
import com.docmgmt.document_qa_app.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_success() {
        // Arrange
        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO();
        jwtRequestDTO.setUsername("testuser");
        jwtRequestDTO.setPassword("testpassword");

        ResponseEntity<JwtResponse> jwtResponse = new ResponseEntity<>(new JwtResponse("test-jwt-token","testuser"),HttpStatus.OK);
        when(authService.authenticateUser(jwtRequestDTO)).thenReturn(jwtResponse);

        // Act
        ResponseEntity<JwtResponse> response = (ResponseEntity<JwtResponse>) authController.login(jwtRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getUsername());
        verify(authService, times(1)).authenticateUser(jwtRequestDTO);
    }

    @Test
    void login_fail_bad_credentials() {
        // Arrange
        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO();
        jwtRequestDTO.setUsername("testuser");
        jwtRequestDTO.setPassword("testpassword");

        when(authService.authenticateUser(jwtRequestDTO)).thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act
        ResponseEntity<String> response = (ResponseEntity<String>) authController.login(jwtRequestDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid username or password", response.getBody());
        verify(authService, times(1)).authenticateUser(jwtRequestDTO);
    }

    @Test
    void login_fail_runtime_error() {
        // Arrange
        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO();
        jwtRequestDTO.setUsername("testuser");
        jwtRequestDTO.setPassword("testpassword");

        when(authService.authenticateUser(jwtRequestDTO)).thenThrow(new RuntimeException("Db Error"));

        // Act
        ResponseEntity<String> response = (ResponseEntity<String>) authController.login(jwtRequestDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error: Db Error", response.getBody());
        verify(authService, times(1)).authenticateUser(jwtRequestDTO);
    }

    @Test
    void registerUserAccount_success() {
        // Arrange
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setPassword("testpassword");
        userRegistrationDTO.setUsername("testuser");

        // Act
        ResponseEntity<String> response = authController.registerUserAccount(userRegistrationDTO);

        // Assert
        verify(userService, Mockito.times(1)).save(userRegistrationDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void registerUserAccount_fail() {
        // Arrange
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setPassword("testpassword");
        userRegistrationDTO.setUsername("testuser");

        when(userService.save(userRegistrationDTO)).thenThrow(new RuntimeException("Db Error"));

        // Act
        ResponseEntity<String> response = authController.registerUserAccount(userRegistrationDTO);

        // Assert
        verify(userService, Mockito.times(1)).save(userRegistrationDTO);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Cannot register this user: Db Error", response.getBody());
    }
}