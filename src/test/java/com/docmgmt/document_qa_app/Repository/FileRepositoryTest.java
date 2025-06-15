package com.docmgmt.document_qa_app.Repository;

import com.docmgmt.document_qa_app.Model.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    Long file1Id;
    Long file2Id;

    @BeforeEach
    void setUp() {
        // Create FileEntity objects with all required fields including content
        FileEntity file1 = new FileEntity();
        file1.setTitle("test title1");
        file1.setKeyword("test Keyword1");
        file1.setFilename("test1.txt");
        file1.setContentType("text/plain");
        file1.setDescription("testDescription1");
        file1.setEditorId(2L);
        file1.setFileSize(500L);
        file1.setData("Hello1".getBytes());
        file1.setContent("Hello1"); // Set the content field
        file1.setUploadTime(LocalDateTime.now()); // Set upload time if required

        FileEntity file2 = new FileEntity();
        file2.setTitle("test title2");
        file2.setKeyword("test Keyword2");
        file2.setFilename("test2.txt");
        file2.setContentType("text/plain");
        file2.setDescription("testDescription2");
        file2.setEditorId(3L);
        file2.setFileSize(550L);
        file2.setData("Hello2".getBytes());
        file2.setContent("Hello2"); // Set the content field
        file2.setUploadTime(LocalDateTime.now()); // Set upload time if required

        fileRepository.save(file1);
        fileRepository.save(file2);
        file1Id = file1.getId();
        file2Id = file2.getId();
    }

    @Test
    void findByEditorId() {
        // Act
        Optional<Page<FileEntity>> results = fileRepository.findByEditorId(2L, pageable());

        // Assert
        assertTrue(results.isPresent());
        assertFalse(results.get().getContent().isEmpty());

        FileEntity foundFile = results.get().getContent().get(0);
        assertEquals("text/plain", foundFile.getContentType());
        assertEquals("test title1", foundFile.getTitle());
        assertEquals("test Keyword1", foundFile.getKeyword());
        assertEquals(500L, foundFile.getFileSize());
        assertEquals("testDescription1", foundFile.getDescription());
        assertEquals("test1.txt", foundFile.getFilename());
        assertEquals(2L, foundFile.getEditorId());
    }

    @Test
    void findById() {
        // Act
        Optional<FileEntity> results = fileRepository.findById(file2Id);

        // Assert
        assertTrue(results.isPresent());

        FileEntity foundFile = results.get();
        assertEquals("text/plain", foundFile.getContentType());
        assertEquals("test title2", foundFile.getTitle());
        assertEquals("test Keyword2", foundFile.getKeyword());
        assertEquals(550L, foundFile.getFileSize());
        assertEquals("testDescription2", foundFile.getDescription());
        assertEquals("test2.txt", foundFile.getFilename());
        assertEquals(3L, foundFile.getEditorId());
    }

    @Test
    void findByFileType() {
        // Act
        Optional<Page<FileEntity>> results = fileRepository.findByFileType("text/plain", pageable());

        // Assert
        assertTrue(results.isPresent());
        assertEquals(2, results.get().getTotalElements());
        assertFalse(results.get().getContent().isEmpty());

        FileEntity foundFile = results.get().getContent().get(0);
        assertEquals("text/plain", foundFile.getContentType());
        assertEquals("test title1", foundFile.getTitle());
        assertEquals("test Keyword1", foundFile.getKeyword());
        assertEquals(500L, foundFile.getFileSize());
        assertEquals("testDescription1", foundFile.getDescription());
        assertEquals("test1.txt", foundFile.getFilename());
        assertEquals(2L, foundFile.getEditorId());
    }

    @Test
    void findByKeyword() {
        // Act
        Optional<Page<FileEntity>> results = fileRepository.findByKeyword("test Keyword1", pageable());

        // Assert
        assertTrue(results.isPresent());
        assertFalse(results.get().getContent().isEmpty());

        FileEntity foundFile = results.get().getContent().get(0);
        assertEquals("text/plain", foundFile.getContentType());
        assertEquals("test title1", foundFile.getTitle());
        assertEquals("test Keyword1", foundFile.getKeyword());
        assertEquals(500L, foundFile.getFileSize());
        assertEquals("testDescription1", foundFile.getDescription());
        assertEquals("test1.txt", foundFile.getFilename());
        assertEquals(2L, foundFile.getEditorId());
    }

    public Pageable pageable() {
        Sort sort = Sort.by(Sort.Direction.fromString("ASC"), "id");
        return PageRequest.of(0, 10, sort);
    }
}