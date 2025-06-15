package com.docmgmt.document_qa_app.Model;

import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "files")
@Getter
@Setter
public class FileEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String contentType; // e.g. pdf, docx, xml

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] data;

    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String description;

    @Column(nullable = false)
    private Long editorId;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    public FileEntity(Long id, String title, String keyword, String filename, String contentType, String description, Long editorId, Long fileSize, byte[] data) {
        this();
        this.id = id;
        this.title = title;
        this.keyword = keyword;
        this.filename = filename;
        this.contentType = contentType;
        this.description = description;
        this.editorId = editorId;
        this.fileSize = fileSize;
        this.data = data;
    }

    public FileEntity() {
        this.uploadTime = LocalDateTime.now();
    }

    public FileMetadataDTO toMetadataDTO(FileEntity file) {
        if (file == null) return null;

        FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
        fileMetadataDTO.setId(file.getId());
        fileMetadataDTO.setFilename(file.getFilename());
        fileMetadataDTO.setContentType(file.getContentType());
        fileMetadataDTO.setFileSize(file.getFileSize());
        fileMetadataDTO.setEditorId(file.getEditorId());
        fileMetadataDTO.setDescription(file.getDescription());
        fileMetadataDTO.setKeyword(file.getKeyword());
        fileMetadataDTO.setTitle(file.getTitle());

        return fileMetadataDTO;
    }
}

