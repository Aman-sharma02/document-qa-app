package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.JwtRequestDTO;
import com.docmgmt.document_qa_app.Model.JwtResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<JwtResponse> authenticateUser(JwtRequestDTO loginRequest);
}
