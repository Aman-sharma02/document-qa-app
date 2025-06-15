package com.docmgmt.document_qa_app.Service;

import com.docmgmt.document_qa_app.Model.DTO.JwtRequestDTO;
import com.docmgmt.document_qa_app.Model.JwtResponse;
import com.docmgmt.document_qa_app.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public ResponseEntity<JwtResponse> authenticateUser(JwtRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Object userPrincipal = authentication.getPrincipal();
        String username="";
        if(userPrincipal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }

        return new ResponseEntity<>(JwtResponse.builder()
                .token(jwt)
                .username(username)
                .build(), HttpStatus.OK);
    }
}
