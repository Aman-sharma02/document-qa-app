package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.UpdateRolesDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserDetailsDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserRegistrationDTO;
import com.docmgmt.document_qa_app.Model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User save(UserRegistrationDTO userRegistrationDTO);

    ResponseEntity<String> updateRole(UpdateRolesDTO updateRolesDTO);

    ResponseEntity<String> deleteUser(Long id);

    ResponseEntity<List<UserDetailsDTO>> getAllUsers();

    ResponseEntity<UserDetailsDTO> getUserById(Long id);
}
