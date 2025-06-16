package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.FileDTO;
import com.docmgmt.document_qa_app.Model.DTO.FileMetadataDTO;
import com.docmgmt.document_qa_app.Model.FileEntity;
import com.docmgmt.document_qa_app.Model.PagedResponse;
import com.docmgmt.document_qa_app.Model.User;
import com.docmgmt.document_qa_app.Repository.FileRepository;
import com.docmgmt.document_qa_app.Repository.UserRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final String FILE_CACHE = "fileCache";
    private static final String FILE_METADATA_CACHE = "fileMetadataCache";
    private static final String USER_CACHE = "userCache";
    private static final String SEARCH_CACHE = "searchCache";
    private static final String PAGED_RESPONSE_CACHE = "pagedResponseCache";

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository) {
        super();
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FILE_CACHE, allEntries = true),
            @CacheEvict(value = FILE_METADATA_CACHE, allEntries = true),
            @CacheEvict(value = PAGED_RESPONSE_CACHE, allEntries = true),
            @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    })
    public String uploadFile(FileDTO file, UserDetails userDetails) throws IOException, TikaException {
        String username = userDetails.getUsername();
        Tika tika = new Tika();
        String content = "";
        try (InputStream inputStream = file.getFile().getInputStream()) {
            content = tika.parseToString(inputStream);
        }
        file.setContent(content);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + " not found"));
        fileRepository.save(toFileEntity(file, user.getId()));
        return "File uploaded successfully: " + file.getFile().getOriginalFilename();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FILE_CACHE, key = "'file:' + #id"),
            @CacheEvict(value = FILE_METADATA_CACHE, key = "'metadata:' + #id"),
            @CacheEvict(value = PAGED_RESPONSE_CACHE, allEntries = true),
            @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    })
    public String updateFile(FileDTO newFile, Long id, UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        FileEntity oldFile = fileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id: " + id));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + " not found"));
        if (Objects.equals(user.getId(), oldFile.getEditorId())) {
            FileEntity newFileEntity = toFileEntity(newFile, user.getId());
            newFileEntity.setId(oldFile.getId());
            fileRepository.save(newFileEntity);
            return "File updated successfully: " + newFile.getFile().getOriginalFilename();
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Editors can update file");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = FILE_CACHE, key = "'file:' + #id"),
            @CacheEvict(value = FILE_METADATA_CACHE, key = "'metadata:' + #id"),
            @CacheEvict(value = PAGED_RESPONSE_CACHE, allEntries = true),
            @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    })
    public String deleteFile(Long id, UserDetails userDetails) {
        String username = userDetails.getUsername();
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id - " + id));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + username + " not found"));
        if(Objects.equals(user.getId(), file.getEditorId())) {
            fileRepository.delete(file);
            return "File Deleted Successfully";
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Editors can delete file");
        }
    }

    @Override
    public FileEntity findById(Long id) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id - " + id));
        if(file.getData() == null || file.getData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id - " + id);
        }
        return file;
    }

    @Override
    @Cacheable(value = FILE_METADATA_CACHE, key = "'metadata:' + #id")
    public FileMetadataDTO findFileMetaById(Long id) {
        FileEntity file = fileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id - " + id));
        return toMetadataDTO(file);
    }

    @Override
    @Cacheable(value = PAGED_RESPONSE_CACHE,
            key = "'editor:' + #editorId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PagedResponse<FileMetadataDTO> findFileMetaByEditorId(Long editorId, Pageable pageable) {
        Page<FileEntity> files = fileRepository.findByEditorId(editorId, pageable).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No files found with EditorId - " + editorId));
        if(files.getTotalElements() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No files found with EditorId - " + editorId);
        return toPagedResponse(files);
    }

    @Override
    @Cacheable(value = PAGED_RESPONSE_CACHE,
            key = "'fileType:' + #fileType + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PagedResponse<FileMetadataDTO> findFileMetaByFileType(String fileType, Pageable pageable) {
        Page<FileEntity> files = fileRepository.findByFileType(fileType, pageable).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with fileType - " + fileType));
        if(files.getTotalElements() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No files found with fileType - " + fileType);
        return toPagedResponse(files);
    }

    @Override
    @Cacheable(value = PAGED_RESPONSE_CACHE,
            key = "'keyword:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PagedResponse<FileMetadataDTO> findFileMetaByKeyword(String keyword, Pageable pageable) {
        Page<FileEntity> files = fileRepository.findByKeyword(keyword, pageable).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with keyword - " + keyword));
        if(files.getTotalElements() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No files found with keyword - " + keyword);
        return toPagedResponse(files);
    }

    @Override
    @Cacheable(value = SEARCH_CACHE,
            key = "'question:' + #question.hashCode() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PagedResponse<FileMetadataDTO> answerQuestion(String question, Pageable pageable) {

        Set<String> wordsSet = Arrays.stream(question.split("\\s+"))
                .map(String::toLowerCase)
                .filter(word -> word.length() >= 3)
                .collect(Collectors.toSet());
        if (wordsSet.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid Question");
        }

        List<FileEntity> allFileEntities = new ArrayList<>();
        for (String word : wordsSet) {
            Pageable largePageable = PageRequest.of(0, Integer.MAX_VALUE);
            Optional<Page<FileEntity>> pageResult = fileRepository.findByKeyword(word, largePageable);
            pageResult.ifPresent(fileEntities -> allFileEntities.addAll(fileEntities.getContent()));
        }

        if(allFileEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Files found");
        }

        List<FileMetadataDTO> uniqueFiles = new ArrayList<>(allFileEntities.stream()
                .map(this::toMetadataDTO)
                .collect(Collectors.toMap(
                        FileMetadataDTO::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), uniqueFiles.size());

        List<FileMetadataDTO> paginatedResult = start >= uniqueFiles.size()
                ? Collections.emptyList()
                : uniqueFiles.subList(start, end);

        Page<FileMetadataDTO> files = new PageImpl<>(paginatedResult, pageable, uniqueFiles.size());
        return new PagedResponse<>(
                files.getContent().stream().toList(),
                files.getNumber(),
                files.getSize(),
                files.getTotalElements(),
                files.getTotalPages(),
                files.isFirst(),
                files.isLast());
    }

    @Override
    @CacheEvict(value = {FILE_CACHE, FILE_METADATA_CACHE, PAGED_RESPONSE_CACHE, SEARCH_CACHE, USER_CACHE},
            allEntries = true)
    public String clearCache() {
        return "All Caches has been cleared";
    }

    public ResponseEntity<String> getCacheStats() {
        Long dbSize = redisTemplate.execute(RedisConnection::dbSize);;
        return ResponseEntity.ok("Cache contains " + dbSize + " keys");
    }

    private FileEntity toFileEntity(FileDTO file, Long userId) throws IOException {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilename(file.getFile().getOriginalFilename());
        fileEntity.setContentType(file.getFile().getContentType());
        fileEntity.setData(file.getFile().getBytes());
        fileEntity.setContent(file.getContent());
        fileEntity.setFileSize(file.getFile().getSize());
        fileEntity.setEditorId(userId);
        fileEntity.setDescription(file.getDescription());
        fileEntity.setKeyword(file.getKeyword());
        fileEntity.setTitle(file.getTitle());
        return fileEntity;
    }

    private FileMetadataDTO toMetadataDTO(FileEntity file) {
        if (file == null) return null;

        FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
        fileMetadataDTO.setId(file.getId());
        fileMetadataDTO.setContent(file.getContent());
        fileMetadataDTO.setFilename(file.getFilename());
        fileMetadataDTO.setContentType(file.getContentType());
        fileMetadataDTO.setFileSize(file.getFileSize());
        fileMetadataDTO.setEditorId(file.getEditorId());
        fileMetadataDTO.setDescription(file.getDescription());
        fileMetadataDTO.setKeyword(file.getKeyword());
        fileMetadataDTO.setTitle(file.getTitle());

        return fileMetadataDTO;
    }

    private PagedResponse<FileMetadataDTO> toPagedResponse(Page<FileEntity> files) {
        List<FileMetadataDTO> fileMetadataDTOS = files.getContent().stream()
                .map(this::toMetadataDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                fileMetadataDTOS,
                files.getNumber(),
                files.getSize(),
                files.getTotalElements(),
                files.getTotalPages(),
                files.isFirst(),
                files.isLast()
        );
    }

}
