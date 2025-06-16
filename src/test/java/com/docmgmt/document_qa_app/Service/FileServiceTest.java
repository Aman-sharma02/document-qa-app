package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.FileDTO;
import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;
import com.docmgmt.document_qa_app.Repository.FileRepository;
import com.docmgmt.document_qa_app.Repository.UserRepository;
import com.docmgmt.document_qa_app.Model.PagedResponse;
import com.docmgmt.document_qa_app.Model.FileEntity;
import com.docmgmt.document_qa_app.Model.User;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FileServiceImpl fileService;

    private User testUser;
    private FileEntity testFileEntity;
    private FileDTO testFileDTO;
    private MockMultipartFile mockFile;
    UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUsername() {
                return "testuser";
            }
        };
        // Setup test file entity
        testFileEntity = new FileEntity();
        testFileEntity.setId(1L);
        testFileEntity.setFilename("test.txt");
        testFileEntity.setContentType("text/plain");
        testFileEntity.setData("test content".getBytes());
        testFileEntity.setContent("test content");
        testFileEntity.setFileSize(12L);
        testFileEntity.setEditorId(1L);
        testFileEntity.setDescription("Test file");
        testFileEntity.setKeyword("test");
        testFileEntity.setTitle("Test Title");

        // Setup mock multipart file
        mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // Setup test file DTO
        testFileDTO = new FileDTO();
        testFileDTO.setFile(mockFile);
        testFileDTO.setDescription("Test file");
        testFileDTO.setKeyword("test");
        testFileDTO.setTitle("Test Title");
    }

    @Test
    void uploadFile_Success() throws IOException, TikaException {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFileEntity);

        // Act
        String result = fileService.uploadFile(testFileDTO, userDetails);

        // Assert
        assertEquals("File uploaded successfully: test.txt", result);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void uploadFile_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.uploadFile(testFileDTO, userDetails));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User: testuser not found"));
    }

    @Test
    void updateFile_Success() throws IOException {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFileEntity);

        // Act
        String result = fileService.updateFile(testFileDTO, 1L, userDetails);

        // Assert
        assertEquals("File updated successfully: test.txt", result);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void updateFile_FileNotFound() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.updateFile(testFileDTO, 1L, userDetails));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateFile_Forbidden() throws IOException {
        // Arrange
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(differentUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.updateFile(testFileDTO, 1L, userDetails));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Only Editors can update file"));
    }

    @Test
    void deleteFile_Success() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        String result = fileService.deleteFile(1L, userDetails);

        // Assert
        assertEquals("File Deleted Successfully", result);
        verify(fileRepository, times(1)).delete(testFileEntity);
    }

    @Test
    void deleteFile_Forbidden() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(differentUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.deleteFile(1L, userDetails));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void findById_Success() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));

        // Act
        FileEntity result = fileService.findById(1L);

        // Assert
        assertEquals(testFileEntity, result);
        assertEquals(1L, result.getId());
        assertEquals("test.txt", result.getFilename());
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.findById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findById_EmptyData() {
        // Arrange
        testFileEntity.setData(new byte[0]);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.findById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findFileMetaById_Success() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFileEntity));

        // Act
        FileMetadataDTO result = fileService.findFileMetaById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test.txt", result.getFilename());
        assertEquals("test content", result.getContent());
    }

    @Test
    void findFileMetaByEditorId_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileEntity> page = new PageImpl<>(Arrays.asList(testFileEntity), pageable, 1);
        when(fileRepository.findByEditorId(1L, pageable)).thenReturn(Optional.of(page));

        // Act
        PagedResponse<FileMetadataDTO> result = fileService.findFileMetaByEditorId(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findFileMetaByEditorId_NoFiles() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(fileRepository.findByEditorId(1L, pageable)).thenReturn(Optional.of(emptyPage));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.findFileMetaByEditorId(1L, pageable));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findFileMetaByFileType_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileEntity> page = new PageImpl<>(Arrays.asList(testFileEntity), pageable, 1);
        when(fileRepository.findByFileType("text/plain", pageable)).thenReturn(Optional.of(page));

        // Act
        PagedResponse<FileMetadataDTO> result = fileService.findFileMetaByFileType("text/plain", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findFileMetaByKeyword_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileEntity> page = new PageImpl<>(Arrays.asList(testFileEntity), pageable, 1);
        when(fileRepository.findByKeyword("test", pageable)).thenReturn(Optional.of(page));

        // Act
        PagedResponse<FileMetadataDTO> result = fileService.findFileMetaByKeyword("test", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void answerQuestion_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable largePageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<FileEntity> page = new PageImpl<>(Arrays.asList(testFileEntity), largePageable, 1);
        when(fileRepository.findByKeyword("test", largePageable)).thenReturn(Optional.of(page));

        // Act
        PagedResponse<FileMetadataDTO> result = fileService.answerQuestion("test question", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void answerQuestion_InvalidQuestion() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.answerQuestion("ab", pageable));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Not a valid Question"));
    }

    @Test
    void answerQuestion_NoFilesFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable largePageable = PageRequest.of(0, Integer.MAX_VALUE);
        when(fileRepository.findByKeyword("test", largePageable)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.answerQuestion("test question", pageable));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void clearCache_Success() {
        // Act
        String result = fileService.clearCache();

        // Assert
        assertEquals("All Caches has been cleared", result);
    }

    @Test
    void getCacheStats_Success() {
        // Arrange
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(10L);

        // Act
        ResponseEntity<String> result = fileService.getCacheStats();

        // Assert
        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("10 keys"));
    }

    // Additional edge case tests

    @Test
    void answerQuestion_PaginationEdgeCase() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 10); // Second page
        Pageable largePageable = PageRequest.of(0, Integer.MAX_VALUE);

        // Create multiple test files
        FileEntity file1 = new FileEntity();
        file1.setId(1L);
        file1.setFilename("file1.txt");
        FileEntity file2 = new FileEntity();
        file2.setId(2L);
        file2.setFilename("file2.txt");

        Page<FileEntity> page = new PageImpl<>(Arrays.asList(file1, file2), largePageable, 2);
        when(fileRepository.findByKeyword("test", largePageable)).thenReturn(Optional.of(page));

        // Act
        PagedResponse<FileMetadataDTO> result = fileService.answerQuestion("test question", pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty()); // Second page should be empty for only 2 items
    }

    @Test
    void findFileMetaById_NullFile() {
        // Arrange
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> fileService.findFileMetaById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}