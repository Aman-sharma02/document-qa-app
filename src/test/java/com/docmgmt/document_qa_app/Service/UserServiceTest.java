package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.UpdateRolesDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserDetailsDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserRegistrationDTO;
import com.docmgmt.document_qa_app.Model.User;
import com.docmgmt.document_qa_app.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Argon2PasswordEncoder argon2PasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationDTO userRegistrationDTO;
    private UpdateRolesDTO updateRolesDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("VIEWER");

        // Setup user registration DTO
        userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername("newuser");
        userRegistrationDTO.setPassword("plainPassword");

        // Setup update roles DTO
        updateRolesDTO = new UpdateRolesDTO();
        updateRolesDTO.setUsername("testuser");
        updateRolesDTO.setRole("EDITOR");
    }

    @Test
    void save_Success() {
        // Arrange
        when(argon2PasswordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.save(userRegistrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);

        // Verify that save was called with correct user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("VIEWER", savedUser.getRole());

        verify(argon2PasswordEncoder).encode("plainPassword");
    }

    @Test
    void save_WithNullValues() {
        // Arrange
        UserRegistrationDTO nullDTO = new UserRegistrationDTO();
        nullDTO.setUsername(null);
        nullDTO.setPassword(null);

        when(argon2PasswordEncoder.encode(null)).thenReturn("encodedNull");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.save(nullDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(argon2PasswordEncoder).encode(null);
    }

    @Test
    void updateRole_Success() {
        // Arrange
        when(userRepository.updateRole("testuser", "EDITOR")).thenReturn(1);

        // Act
        ResponseEntity<String> result = userService.updateRole(updateRolesDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User role updated successfully records affected: 1", result.getBody());
        verify(userRepository).updateRole("testuser", "EDITOR");
    }

    @Test
    void updateRole_UserNotFound() {
        // Arrange
        when(userRepository.updateRole("testuser", "EDITOR")).thenReturn(0);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(updateRolesDTO)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User: testuser not found"));
        verify(userRepository).updateRole("testuser", "EDITOR");
    }

    @Test
    void updateRole_MultipleRecordsAffected() {
        // Arrange
        when(userRepository.updateRole("testuser", "EDITOR")).thenReturn(2);

        // Act
        ResponseEntity<String> result = userService.updateRole(updateRolesDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User role updated successfully records affected: 2", result.getBody());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        ResponseEntity<String> result = userService.deleteUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User Deleted Successfully", result.getBody());
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id - 1"));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole("EDITOR");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        ResponseEntity<List<UserDetailsDTO>> result = userService.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());

        UserDetailsDTO firstUser = result.getBody().get(0);
        assertEquals(1L, firstUser.getId());
        assertEquals("testuser", firstUser.getUsername());
        assertEquals("VIEWER", firstUser.getRole());

        UserDetailsDTO secondUser = result.getBody().get(1);
        assertEquals(2L, secondUser.getId());
        assertEquals("user2", secondUser.getUsername());
        assertEquals("EDITOR", secondUser.getRole());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getAllUsers()
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No user present in DB", exception.getReason());
        verify(userRepository).findAll();
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER")));
        assertEquals(1, result.getAuthorities().size());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("Invalid username or password for user - nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_DifferentRoles() {
        // Arrange
        testUser.setRole("ADMIN");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("testuser");

        // Assert
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserDetailsDTO> result = userService.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("testuser", result.getBody().getUsername());
        assertEquals("VIEWER", result.getBody().getRole());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id - 999"));
        verify(userRepository).findById(999L);
    }

    @Test
    void toUserDetailsDTO_ConversionTest() {
        // This tests the private method indirectly through getUserById
        // Arrange
        User userWithAllFields = new User();
        userWithAllFields.setId(5L);
        userWithAllFields.setUsername("fulluser");
        userWithAllFields.setRole("ADMIN");

        when(userRepository.findById(5L)).thenReturn(Optional.of(userWithAllFields));

        // Act
        ResponseEntity<UserDetailsDTO> result = userService.getUserById(5L);

        // Assert
        UserDetailsDTO dto = result.getBody();
        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("fulluser", dto.getUsername());
        assertEquals("ADMIN", dto.getRole());
    }

    // Edge cases and boundary tests

    @Test
    void updateRole_WithEmptyUsername() {
        // Arrange
        UpdateRolesDTO emptyUsernameDTO = new UpdateRolesDTO();
        emptyUsernameDTO.setUsername("");
        emptyUsernameDTO.setRole("EDITOR");

        when(userRepository.updateRole("", "EDITOR")).thenReturn(0);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(emptyUsernameDTO)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateRole_WithNullRole() {
        // Arrange
        UpdateRolesDTO nullRoleDTO = new UpdateRolesDTO();
        nullRoleDTO.setUsername("testuser");
        nullRoleDTO.setRole(null);

        when(userRepository.updateRole("testuser", null)).thenReturn(1);

        // Act
        ResponseEntity<String> result = userService.updateRole(nullRoleDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userRepository).updateRole("testuser", null);
    }

    @Test
    void loadUserByUsername_WithNullUsername() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(null)
        );

        assertTrue(exception.getMessage().contains("Invalid username or password for user - null"));
    }

    @Test
    void deleteUser_WithNegativeId() {
        // Arrange
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(-1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id - -1"));
    }

    @Test
    void getUserById_WithZeroId() {
        // Arrange
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserById(0L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id - 0"));
    }
}