package com.docmgmt.document_qa_app.Repository;

import com.docmgmt.document_qa_app.Model.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findById(Long id);

    @Transactional
    @Query("SELECT f FROM FileEntity f WHERE f.editorId = :editorId")
    Optional<Page<FileEntity>> findByEditorId(@Param("editorId") Long editorId, Pageable pageable);

    @Transactional
    @Query("SELECT f FROM FileEntity f WHERE f.contentType ILIKE %:fileType%")
    Optional<Page<FileEntity>> findByFileType(String fileType, Pageable pageable);

    @Transactional
    @Query("SELECT f FROM FileEntity f WHERE f.keyword ILIKE %:keyword%")
    Optional<Page<FileEntity>> findByKeyword(String keyword, Pageable pageable);
}
