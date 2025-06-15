package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;
import com.docmgmt.document_qa_app.Model.FileEntity;
import com.docmgmt.document_qa_app.Model.PagedResponse;
import com.docmgmt.document_qa_app.Service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private ViewController viewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(viewController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSearchFileById_Success() throws Exception {
        // Arrange
        Long fileId = 1L;
        FileEntity file = createSampleFileEntity();
        when(fileService.findById(fileId)).thenReturn(file);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"test-document.pdf\""))
                .andExpect(header().longValue("Content-Length", file.getData().length))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Expires", "Thu, 01 Jan 1970 00:00:00 GMT"))
                .andExpect(content().bytes(file.getData()));

        verify(fileService, times(1)).findById(fileId);
    }

    @Test
    void testSearchFileById_UnknownContentType() throws Exception {
        // Arrange
        Long fileId = 1L;
        FileEntity file = createSampleFileEntity();
        file.setContentType("");
        when(fileService.findById(fileId)).thenReturn(file);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(file.getData()));

        verify(fileService, times(1)).findById(fileId);
    }

    @Test
    void testSearchFileById_ResponseStatusException() throws Exception {
        // Arrange
        Long fileId = 1L;
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        when(fileService.findById(fileId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/{id}", fileId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot fetch file: File not found"));

        verify(fileService, times(1)).findById(fileId);
    }

    @Test
    void testSearchFileById_GenericException() throws Exception {
        // Arrange
        Long fileId = 1L;
        when(fileService.findById(fileId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/fileviewer/{id}", fileId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch file: Database error"));

        verify(fileService, times(1)).findById(fileId);
    }

    @Test
    void testSearchFileMetadataById_Success() throws Exception {
        // Arrange
        Long fileId = 1L;
        when(fileService.findFileMetaById(fileId)).thenReturn(createFileMetadataDTO());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/meta/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"title\":\"Title\",\"content\":\"content\",\"keyword\":\"keyword\",\"filename\":\"document.pdf\",\"contentType\":\"application/pdf\",\"description\":\"description\",\"editorId\":101,\"fileSize\":2048,\"uploadTime\":[2023,12,25,8,10]}"));

        verify(fileService, times(1)).findFileMetaById(fileId);
    }

    @Test
    void testSearchFileMetadataById_ResponseStatusException() throws Exception {
        // Arrange
        Long fileId = 1L;
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Metadata not found");
        when(fileService.findFileMetaById(fileId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/meta/{id}", fileId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot fetch file metadata: Metadata not found"));

        verify(fileService, times(1)).findFileMetaById(fileId);
    }

    @Test
    void testSearchFileMetadataById_GenericException() throws Exception {
        // Arrange
        Long fileId = 1L;
        when(fileService.findFileMetaById(fileId)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/fileviewer/meta/{id}", fileId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch file metadata: Service error"));

        verify(fileService, times(1)).findFileMetaById(fileId);
    }

    @Test
    void testSearchFileByEditorId_Success_DefaultParameters() throws Exception {
        // Arrange
        Long editorId = 1L;
        when(fileService.findFileMetaByEditorId(eq(editorId), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/editor/{editorId}", editorId))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByEditorId(eq(editorId), argThat(pageable ->
                pageable.getPageNumber() == 0 &&
                        pageable.getPageSize() == 10 &&
                        pageable.getSort().getOrderFor("id").getDirection() == Sort.Direction.ASC
        ));
    }

    @Test
    void testSearchFileByEditorId_Success_CustomParameters() throws Exception {
        // Arrange
        Long editorId = 1L;
        when(fileService.findFileMetaByEditorId(eq(editorId), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/editor/{editorId}", editorId)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "filename")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByEditorId(eq(editorId), argThat(pageable ->
                pageable.getPageNumber() == 1 &&
                        pageable.getPageSize() == 5 &&
                        pageable.getSort().getOrderFor("filename").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    void testSearchFileByEditorId_ResponseStatusException() throws Exception {
        // Arrange
        Long editorId = 1L;
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        when(fileService.findFileMetaByEditorId(eq(editorId), any(Pageable.class))).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/editor/{editorId}", editorId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Cannot fetch file metadata: Access denied"));

        verify(fileService, times(1)).findFileMetaByEditorId(eq(editorId), any(Pageable.class));
    }

    @Test
    void testSearchFileByEditorId_GenericException() throws Exception {
        // Arrange
        Long editorId = 1L;
        when(fileService.findFileMetaByEditorId(eq(editorId), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/fileviewer/editor/{editorId}", editorId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch file metadata: Database connection failed"));

        verify(fileService, times(1)).findFileMetaByEditorId(eq(editorId), any(Pageable.class));
    }

    @Test
    void testSearchFileByType_Success() throws Exception {
        // Arrange
        String fileType = "pdf";
        when(fileService.findFileMetaByFileType(eq(fileType), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/fileType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fileType))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByFileType(eq(fileType), any(Pageable.class));
    }

    @Test
    void testSearchFileByType_CustomPagination() throws Exception {
        // Arrange
        String fileType = "docx";
        when(fileService.findFileMetaByFileType(eq(fileType), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/fileType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fileType)
                        .param("page", "2")
                        .param("size", "20")
                        .param("sortBy", "createdDate")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByFileType(eq(fileType), argThat(pageable ->
                pageable.getPageNumber() == 2 &&
                        pageable.getPageSize() == 20 &&
                        pageable.getSort().getOrderFor("createdDate").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    void testSearchFileByType_GenericException() throws Exception {
        // Arrange
        String fileType = "pdf";

        when(fileService.findFileMetaByFileType(anyString(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Search service error"));

        // Act & Assert
        mockMvc.perform(get("/fileviewer/fileType")
                        .content(fileType) // Correct way to pass query param in GET
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch file metadata: Search service error"));

        verify(fileService, times(1)).findFileMetaByFileType(eq(fileType), any(Pageable.class));
    }

    @Test
    void testSearchFileByKeyword_Success() throws Exception {
        // Arrange
        String keyword = "contract";
        when(fileService.findFileMetaByKeyword(eq(keyword), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/keyword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(keyword))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByKeyword(eq(keyword), any(Pageable.class));
    }

    @Test
    void testSearchFileByKeyword_CustomPagination() throws Exception {
        // Arrange
        String keyword = "invoice";
        when(fileService.findFileMetaByKeyword(eq(keyword), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/keyword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(keyword)
                        .param("page", "1")
                        .param("size", "15")
                        .param("sortBy", "title")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByKeyword(eq(keyword), argThat(pageable ->
                pageable.getPageNumber() == 1 &&
                        pageable.getPageSize() == 15 &&
                        pageable.getSort().getOrderFor("title").getDirection() == Sort.Direction.ASC
        ));
    }

    @Test
    void testSearchFileByKeyword_ResponseStatusException() throws Exception {
        // Arrange
        String keyword = "test";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "No files found");
        when(fileService.findFileMetaByKeyword(eq(keyword), any(Pageable.class))).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/keyword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(keyword))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot fetch file metadata: No files found"));

        verify(fileService, times(1)).findFileMetaByKeyword(eq(keyword), any(Pageable.class));
    }

    @Test
    void testSearchFileByKeyword_GenericException() throws Exception {
        // Arrange
        String keyword = "report";

        when(fileService.findFileMetaByKeyword(anyString(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Keyword search failed"));

        // Act & Assert
        mockMvc.perform(get("/fileviewer/keyword")
                        .content(keyword)// Better than sending raw JSON for GET
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot fetch file metadata: Keyword search failed"));

        verify(fileService, times(1)).findFileMetaByKeyword(eq(keyword), any(Pageable.class));
    }

    @Test
    void testSearchFileById_FilenameWithSpecialCharacters() throws Exception {
        // Arrange
        Long fileId = 1L;
        FileEntity file = createSampleFileEntity();
        file.setFilename("test file with spaces & symbols.pdf");
        when(fileService.findById(fileId)).thenReturn(file);

        // Act & Assert
        mockMvc.perform(get("/fileviewer/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "form-data; name=\"attachment\"; filename=\"test%20file%20with%20spaces%20%26%20symbols.pdf\""));

        verify(fileService, times(1)).findById(fileId);
    }

    @Test
    void testSearchFileByEditorId_InvalidSortDirection() throws Exception {
        // Arrange
        Long editorId = 1L;
        when(fileService.findFileMetaByEditorId(eq(editorId), any(Pageable.class))).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(get("/fileviewer/editor/{editorId}", editorId)
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk());

        verify(fileService, times(1)).findFileMetaByEditorId(eq(editorId), any(Pageable.class));
    }

    public static FileMetadataDTO createFileMetadataDTO() {
        FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();

        fileMetadataDTO.setId(1L);
        fileMetadataDTO.setTitle("Title");
        fileMetadataDTO.setContent("content");
        fileMetadataDTO.setKeyword("keyword");
        fileMetadataDTO.setFilename("document.pdf");
        fileMetadataDTO.setContentType("application/pdf");
        fileMetadataDTO.setDescription("description");
        fileMetadataDTO.setEditorId(101L);
        fileMetadataDTO.setFileSize(2048L); // 2KB
        fileMetadataDTO.setUploadTime(LocalDateTime.of(2023, 12, 25, 8,10));

        return fileMetadataDTO;
    }

    private PagedResponse<FileMetadataDTO> pagedResponse() {
        List<FileMetadataDTO> fileMetadataDTOs = List.of(createFileMetadataDTO());
        Page<FileMetadataDTO> fileMetadataDTOsPage = new PageImpl<>(fileMetadataDTOs);
        return new PagedResponse<>(
                fileMetadataDTOs,
                fileMetadataDTOsPage.getNumber(),
                fileMetadataDTOsPage.getSize(),
                fileMetadataDTOsPage.getTotalElements(),
                fileMetadataDTOsPage.getTotalPages(),
                fileMetadataDTOsPage.isFirst(),
                fileMetadataDTOsPage.isLast()
        );
    }

    private FileEntity createSampleFileEntity() {
        FileEntity file = new FileEntity();
        file.setId(1L);
        file.setFilename("test-document.pdf");
        file.setContentType("application/pdf");
        file.setData("Sample PDF content".getBytes());
        return file;
    }
}