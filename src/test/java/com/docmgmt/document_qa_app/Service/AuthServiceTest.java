package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.JwtRequestDTO;
import com.docmgmt.document_qa_app.Model.JwtResponse;
import com.docmgmt.document_qa_app.Security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthServiceImpl authService;

    private JwtRequestDTO loginRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        loginRequest = new JwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        userDetails = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")))
                .build();
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        String expectedToken = "jwt-token-12345";
        String expectedUsername = "testuser";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<JwtResponse> response = authService.authenticateUser(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(expectedToken, response.getBody().getToken());
            assertEquals(expectedUsername, response.getBody().getUsername());

            // Verify interactions
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProvider, times(1)).generateToken(authentication);
            verify(securityContext, times(1)).setAuthentication(authentication);
            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, times(1));
        }
    }

    @Test
    void testAuthenticateUser_Success_WithCustomUserDetails() {
        // Arrange - Test with a custom UserDetails implementation
        UserDetails customUserDetails = new UserDetails() {
            @Override
            public String getUsername() {
                return "customuser";
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };

        String expectedToken = "custom-jwt-token";
        String expectedUsername = "customuser";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<JwtResponse> response = authService.authenticateUser(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(expectedToken, response.getBody().getToken());
            assertEquals(expectedUsername, response.getBody().getUsername());
        }
    }

    @Test
    void testAuthenticateUser_PrincipalNotUserDetails() {
        // Arrange - Test when principal is not an instance of UserDetails
        String nonUserDetailsPrincipal = "stringPrincipal";
        String expectedToken = "jwt-token-12345";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(nonUserDetailsPrincipal);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<JwtResponse> response = authService.authenticateUser(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(expectedToken, response.getBody().getToken());
            assertEquals("", response.getBody().getUsername()); // Should be empty string when not UserDetails
        }
    }

    @Test
    void testAuthenticateUser_BadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        // Verify that token generation and security context setting are not called
        verify(tokenProvider, never()).generateToken(any());
        verifyNoInteractions(securityContext);
    }

    @Test
    void testAuthenticateUser_DisabledException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User account is disabled"));

        // Act & Assert
        assertThrows(DisabledException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        // Verify that token generation and security context setting are not called
        verify(tokenProvider, never()).generateToken(any());
        verifyNoInteractions(securityContext);
    }

    @Test
    void testAuthenticateUser_RuntimeException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        // Verify that token generation and security context setting are not called
        verify(tokenProvider, never()).generateToken(any());
        verifyNoInteractions(securityContext);
    }

    @Test
    void testAuthenticateUser_TokenGenerationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication))
                .thenThrow(new RuntimeException("Token generation failed"));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                authService.authenticateUser(loginRequest);
            });

            // Verify that authentication was set in security context before token generation failed
            verify(securityContext, times(1)).setAuthentication(authentication);
        }
    }


    @Test
    void testAuthenticateUser_VerifyAuthenticationTokenCreation() {
        // Arrange
        String expectedToken = "jwt-token-12345";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authService.authenticateUser(loginRequest);

            // Assert - Verify the correct UsernamePasswordAuthenticationToken was created
            verify(authenticationManager, times(1)).authenticate(argThat(authToken -> {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authToken;
                return "testuser".equals(token.getPrincipal()) &&
                        "password123".equals(token.getCredentials());
            }));
        }
    }

    @Test
    void testAuthenticateUser_NullLoginRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            authService.authenticateUser(null);
        });
    }

    @Test
    void testAuthenticateUser_NullUsername() {
        // Arrange
        loginRequest.setUsername(null);

        // Act & Assert
        // This will likely throw an exception during authentication
        assertThrows(Exception.class, () -> {
            authService.authenticateUser(loginRequest);
        });
    }

    @Test
    void testAuthenticateUser_NullPassword() {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        // This will likely throw an exception during authentication
        assertThrows(Exception.class, () -> {
            authService.authenticateUser(loginRequest);
        });
    }

    @Test
    void testAuthenticateUser_EmptyCredentials() {
        // Arrange
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Empty credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateUser(loginRequest);
        });
    }

    @Test
    void testAuthenticateUser_VerifySecurityContextInteraction() {
        // Arrange
        String expectedToken = "jwt-token-12345";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            authService.authenticateUser(loginRequest);

            // Assert - Verify SecurityContextHolder interactions
            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, times(1));
            verify(securityContext, times(1)).setAuthentication(authentication);
        }
    }
}