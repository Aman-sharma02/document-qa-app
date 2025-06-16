package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.FileDTO;
import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;

import com.docmgmt.document_qa_app.Model.FileEntity;
import com.docmgmt.document_qa_app.Model.PagedResponse;
import org.apache.tika.exception.TikaException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public interface FileService {

    String uploadFile(FileDTO fileDTO, UserDetails userDetails) throws IOException, TikaException;

    String updateFile(FileDTO fileDTO, Long id, UserDetails userDetails) throws IOException;

    String deleteFile(Long id, UserDetails userDetails) throws IOException;

    FileEntity findById(Long id) throws RuntimeException;

    FileMetadataDTO findFileMetaById(Long id) throws RuntimeException;

    PagedResponse<FileMetadataDTO> findFileMetaByEditorId(Long id, Pageable pageable);

    PagedResponse<FileMetadataDTO> findFileMetaByFileType(String fileType, Pageable pageable);

    PagedResponse<FileMetadataDTO> findFileMetaByKeyword(String keyword, Pageable pageable);

    PagedResponse<FileMetadataDTO> answerQuestion(String question, Pageable pageable);

    String clearCache();

    ResponseEntity<String> getCacheStats();
}
