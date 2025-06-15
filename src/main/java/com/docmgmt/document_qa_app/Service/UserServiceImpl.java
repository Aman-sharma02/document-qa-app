package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.UpdateRolesDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserDetailsDTO;
import com.docmgmt.document_qa_app.Model.DTO.UserRegistrationDTO;
import com.docmgmt.document_qa_app.Model.User;
import com.docmgmt.document_qa_app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private final Argon2PasswordEncoder argon2PasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, @Lazy Argon2PasswordEncoder argon2PasswordEncoder) {
        super();
        this.userRepository = userRepository;
        this.argon2PasswordEncoder = argon2PasswordEncoder;
    }

    @Override
    public User save(UserRegistrationDTO userRegistrationDTO) {
        User user = new User(userRegistrationDTO.getUsername(), argon2PasswordEncoder.encode(userRegistrationDTO.getPassword()), "VIEWER");
        return userRepository.save(user);
    }

    @Override
    public ResponseEntity<String> updateRole(UpdateRolesDTO updateRolesDTO) {
        int result = userRepository.updateRole(updateRolesDTO.getUsername(), updateRolesDTO.getRole());
        if (result == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + updateRolesDTO.getUsername() + " not found");
        } else {
            return new ResponseEntity<>("User role updated successfully records affected: " + result, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<String> deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id - " + id));
        userRepository.delete(user);
        return new ResponseEntity<>("User Deleted Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<UserDetailsDTO>> getAllUsers() {
        List<UserDetailsDTO> users = userRepository.findAll().stream()
                .map(this::toUserDetailsDTO)
                .toList();
        if(users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user present in DB");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid username or password for user - " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    private UserDetailsDTO toUserDetailsDTO(User user) {
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public ResponseEntity<UserDetailsDTO> getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id - " + id));
        return new ResponseEntity<>(toUserDetailsDTO(user), HttpStatus.OK);
    }

}