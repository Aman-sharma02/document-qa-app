package com.docmgmt.document_qa_app.Model.DTO;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class FileMetadataDTO implements Serializable {

    private Long id;

    private String title;

    private String content;

    private String keyword;

    private  String filename;

    private String contentType;

    private String description;

    private Long editorId;

    private Long fileSize;

    private LocalDateTime uploadTime;
}
