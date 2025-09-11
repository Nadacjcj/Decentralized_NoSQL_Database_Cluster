package org.example.node.repository;

import org.example.node.model.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(String email);
}
