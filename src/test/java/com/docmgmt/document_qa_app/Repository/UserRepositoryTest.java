package com.docmgmt.document_qa_app.Repository;

import com.docmgmt.document_qa_app.Model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    Long user1Id;
    Long user2Id;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        User user1 = new User("alice", "encodedpass", "ADMIN");
        User user2 = new User("john", "encodedpass2", "EDITOR");

        userRepository.saveAndFlush(user1);
        userRepository.saveAndFlush(user2);

        user1Id = user1.getId();
        user2Id = user2.getId();
    }

    @Test
    void findByUsername() {

        // Act
        Optional<User> foundUser = userRepository.findByUsername("alice");

        // Assert
        assertNotNull(foundUser);
        assertEquals(foundUser.get().getUsername(),"alice");
        assertEquals(foundUser.get().getRole(), "ADMIN");
        assertEquals(foundUser.get().getPassword(), "encodedpass");
    }

    @Test
    void findById() {
        // Act
        Optional<User> foundUser = userRepository.findById(user1Id);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(foundUser.get().getUsername(),"alice");
        assertEquals(foundUser.get().getRole(), "ADMIN");
        assertEquals(foundUser.get().getPassword(), "encodedpass");
    }

    @Test
    @Transactional
    void updateRole() {
        // Act
        int updatedCount = userRepository.updateRole("alice", "EDITOR");
        userRepository.flush();

        // Assert
        assertEquals(1, updatedCount);

        entityManager.flush();
        entityManager.clear();

        Optional<User> updatedUser = userRepository.findByUsername("alice");
        assertEquals("EDITOR", updatedUser.get().getRole());
    }
}