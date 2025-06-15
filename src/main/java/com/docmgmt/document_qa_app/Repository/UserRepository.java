package com.docmgmt.document_qa_app.Repository;

import com.docmgmt.document_qa_app.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.username = :username")
    int updateRole(@Param("username") String username, @Param("role") String role);

}
