package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.UpdateRolesDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserDetailsDTO;
import com.docmgmt.document_qa_app.Service.FileService;
import com.docmgmt.document_qa_app.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testUpdateUserRole_Success() throws Exception {
        // Arrange
        UpdateRolesDTO updateRolesDTO = new UpdateRolesDTO();
        updateRolesDTO.setUsername("testUser");
        updateRolesDTO.setRole("ADMIN");

        ResponseEntity<String> expectedResponse = new ResponseEntity<>("User role updated successfully records affected: 1", HttpStatus.OK);
        when(userService.updateRole(any(UpdateRolesDTO.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/admin/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(objectMapper.writeValueAsString(updateRolesDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated successfully records affected: 1"));

        verify(userService, times(1)).updateRole(any(UpdateRolesDTO.class));
    }

    @Test
    void testUpdateUserRole_ResponseStatusException() throws Exception {
        // Arrange
        UpdateRolesDTO updateRolesDTO = new UpdateRolesDTO();
        updateRolesDTO.setUsername("testUser");
        updateRolesDTO.setRole("ADMIN");

        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST, "User: " + updateRolesDTO.getUsername() + " not found");
        when(userService.updateRole(any(UpdateRolesDTO.class))).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(put("/admin/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRolesDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot Update role: User: " + updateRolesDTO.getUsername() + " not found"));

        verify(userService, times(1)).updateRole(any(UpdateRolesDTO.class));
    }

    @Test
    void testUpdateUserRole_GenericException() throws Exception {
        // Arrange
        UpdateRolesDTO updateRolesDTO = new UpdateRolesDTO();
        updateRolesDTO.setUsername("testUser");
        updateRolesDTO.setRole("ADMIN");

        when(userService.updateRole(any(UpdateRolesDTO.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(put("/admin/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRolesDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot Update role: Database error"));

        verify(userService, times(1)).updateRole(any(UpdateRolesDTO.class));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("User Deleted Successfully", HttpStatus.OK);
        when(userService.deleteUser(userId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(delete("/admin/delete/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User Deleted Successfully"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_ResponseStatusException() throws Exception {
        // Arrange
        Long userId = 1L;
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id - " + userId);
        when(userService.deleteUser(userId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(delete("/admin/delete/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot Delete user: User not found with id - 1"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_GenericException() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.deleteUser(userId)).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(delete("/admin/delete/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot Delete user: Database connection failed"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        UserDetailsDTO user = UserDetailsDTO.builder().id(1L).role("ADMIN").username("testUser").build();
        List<UserDetailsDTO> users = List.of(user);
        ResponseEntity<List<UserDetailsDTO>> expectedResponse = new ResponseEntity<>(users, HttpStatus.OK);
        when(userService.getAllUsers()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"username\":\"testUser\",\"role\":\"ADMIN\"}]"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_ResponseStatusException() throws Exception {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "No user present in DB");
        when(userService.getAllUsers()).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot fetch users: No user present in DB"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_GenericException() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch users: Service unavailable"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDetailsDTO user = UserDetailsDTO.builder().id(userId).role("ADMIN").username("testUser").build();
        ResponseEntity<UserDetailsDTO> expectedResponse = new ResponseEntity<>(user, HttpStatus.OK);
        when(userService.getUserById(userId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/admin/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"username\":\"testUser\",\"role\":\"ADMIN\"}"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_ResponseStatusException() throws Exception {
        // Arrange
        Long userId = 1L;
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id - " + userId);
        when(userService.getUserById(userId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/admin/user/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot fetch user: User not found with id - 1"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_GenericException() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/admin/user/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch user: Database error"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetCacheStats_Success() throws Exception {
        // Arrange
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Cache stats", HttpStatus.OK);
        when(fileService.getCacheStats()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/admin/CacheStats"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache stats"));

        verify(fileService, times(1)).getCacheStats();
    }

    @Test
    void testGetCacheStats_Exception() throws Exception {
        // Arrange
        when(fileService.getCacheStats()).thenThrow(new RuntimeException("Cache service error"));

        // Act & Assert
        mockMvc.perform(get("/admin/CacheStats"))
                .andExpect(status().isInternalServerError());

        verify(fileService, times(1)).getCacheStats();
    }

    @Test
    void testClearCache_Success() throws Exception {
        // Arrange
        when(fileService.clearCache()).thenReturn("All Caches has been cleared");

        // Act & Assert
        mockMvc.perform(get("/admin/clearCache"))
                .andExpect(status().isOk())
                .andExpect(content().string("All Caches has been cleared"));

        verify(fileService, times(1)).clearCache();
    }

    @Test
    void testClearCache_Exception() throws Exception {
        // Arrange
        when(fileService.clearCache()).thenThrow(new RuntimeException("Failed to clear cache"));

        // Act & Assert
        mockMvc.perform(get("/admin/clearCache"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to clear cache: Failed to clear cache"));

        verify(fileService, times(1)).clearCache();
    }
}