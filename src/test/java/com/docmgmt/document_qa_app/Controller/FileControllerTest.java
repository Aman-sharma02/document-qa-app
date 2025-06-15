package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.FileDTO;
import com.docmgmt.document_qa_app.Service.FileService;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadFileSuccess() throws IOException, TikaException {

        // Arrange
        FileDTO fileDTO = createFileDTO();
        when(fileService.uploadFile(any(FileDTO.class))).thenReturn("File uploaded successfully: test.txt");

        // Act
        ResponseEntity<String> response = fileController.uploadFile(fileDTO);

        // Assert
        verify(fileService, times(1)).uploadFile(fileDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully: test.txt", response.getBody());
    }

    @Test
    void uploadFileFail() throws IOException, TikaException {

        // Arrange
        FileDTO fileDTO = createFileDTO();
        doThrow(new IOException("Exception occurred"))
                .when(fileService).uploadFile(fileDTO);
        // Act
        ResponseEntity<String> response = fileController.uploadFile(fileDTO);

        // Assert
        verify(fileService, times(1)).uploadFile(fileDTO);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Cannot Upload file: Exception occurred", response.getBody());
    }

    @Test
    void updateFileSuccess() throws IOException {
        // Arrange
        FileDTO fileDTO = createFileDTO();
        Long id =1L;
        when(fileService.updateFile(fileDTO, id)).thenReturn("File updated successfully: test.txt");

        //Act
        ResponseEntity<String> response = fileController.updateFile(fileDTO,id);

        // Assert
        verify(fileService, times(1)).updateFile(fileDTO, id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File updated successfully: test.txt", response.getBody());
    }

    @Test
    void updateFileFail() throws IOException {
        // Arrange
        FileDTO fileDTO = createFileDTO();
        Long id =1L;
        when(fileService.updateFile(fileDTO, id)).thenThrow(new IOException("Exception occurred"));

        //Act
        ResponseEntity<String> response = fileController.updateFile(fileDTO, id);

        // Assert
        verify(fileService, times(1)).updateFile(fileDTO, id);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Cannot Update file: Exception occurred", response.getBody());
    }

    @Test
    void deleteFileSuccess() throws IOException {
        // Arrange
        Long id =1L;
        when(fileService.deleteFile(id)).thenReturn("File Deleted Successfully");

        // Act
        ResponseEntity<String> response = fileController.deleteFile(id);

        // Assert
        verify(fileService, times(1)).deleteFile(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File Deleted Successfully", response.getBody());
    }

    @Test
    void deleteFileFail() throws IOException {
        // Arrange
        Long id = 1L;
        when(fileService.deleteFile(id)).thenThrow(new IOException("Exception occurred"));

        // Act
        ResponseEntity<String> response = fileController.deleteFile(id);

        // Assert
        verify(fileService, times(1)).deleteFile(id);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Cannot Delete file: Exception occurred", response.getBody());
    }

    private FileDTO createFileDTO() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFile(multipartFile);
        fileDTO.setTitle("Test Title");
        fileDTO.setDescription("Test Description");
        fileDTO.setKeyword("Test Keyword");
        return fileDTO;
    }
}