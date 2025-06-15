package com.docmgmt.document_qa_app.Controller;

import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;
import com.docmgmt.document_qa_app.Model.PagedResponse;
import com.docmgmt.document_qa_app.Service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QAControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private QAController qaController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(qaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAskQuestion_Success() throws Exception {
        // Arrange
        String question = "question";
        when(fileService.answerQuestion(question, pageable())).thenReturn(pagedResponse());

        // Act & Assert
        mockMvc.perform(post("/qa/ask", question).content(question))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"content\":[{\"id\":1,\"title\":\"Title\",\"content\":\"content\",\"keyword\":\"keyword\",\"filename\":\"document.pdf\",\"contentType\":\"application/pdf\",\"description\":\"description\",\"editorId\":101,\"fileSize\":2048,\"uploadTime\":[1998,11,6,8,40]}],\"page\":0,\"size\":1,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true}"));

        verify(fileService, times(1)).answerQuestion(question, pageable());
    }

    @Test
    void testAskQuestion_fail_no_question() throws Exception {
        // Arrange
        String question = "  ";
        when(fileService.answerQuestion(question, pageable())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid Question"));

        // Act & Assert
        mockMvc.perform(post("/qa/ask", question).content(question))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot answer question: Not a valid Question"));

        verify(fileService, times(1)).answerQuestion(question, pageable());
    }

    @Test
    void testAskQuestion_fail_not_found() throws Exception {
        // Arrange
        String question = "  ";
        when(fileService.answerQuestion(question, pageable())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No Files found"));

        // Act & Assert
        mockMvc.perform(post("/qa/ask", question).content(question))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cannot answer question: No Files found"));

        verify(fileService, times(1)).answerQuestion(question, pageable());
    }

    @Test
    void testAskQuestion_fail_runtime_exception() throws Exception {
        // Arrange
        String question = "  ";
        when(fileService.answerQuestion(question, pageable())).thenThrow(new RuntimeException("An error occurred"));

        // Act & Assert
        mockMvc.perform(post("/qa/ask", question).content(question))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Cannot answer question: An error occurred"));

        verify(fileService, times(1)).answerQuestion(question, pageable());
    }

    private PagedResponse<FileMetadataDTO> pagedResponse() {
        FileMetadataDTO file = new FileMetadataDTO();
        file.setId(1L);
        file.setTitle("Title");
        file.setContent("content");
        file.setKeyword("keyword");
        file.setFilename("document.pdf");
        file.setContentType("application/pdf");
        file.setDescription("description");
        file.setEditorId(101L);
        file.setFileSize(2048L); // 2KB
        file.setUploadTime(LocalDateTime.of(1998, 11, 6, 8,40));

        List<FileMetadataDTO> fileMetadataDTOs = List.of(file);
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

    private Pageable pageable() {
        Sort sort = Sort.by(Sort.Direction.fromString("ASC"), "id");
        return PageRequest.of(0, 10, sort);
    }
}
