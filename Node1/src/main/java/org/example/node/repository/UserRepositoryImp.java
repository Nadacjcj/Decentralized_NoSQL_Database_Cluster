package org.example.node.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.model.User;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImp implements UserRepository{

    private static final String BASE_PATH = "user_data";
    private static final String USER_DATA_PATH = BASE_PATH + "/users_info.json";

    private final ObjectMapper mapper = new ObjectMapper();

    private List<User> readUsers() {
        try {
            File file = new File(USER_DATA_PATH);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users file", e);
        }
    }

    private void writeUsers(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USER_DATA_PATH), users);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users file", e);
        }
    }

    @Override
    public void save(User user) {
        List<User> users = readUsers();
        Optional<User> existing = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(user.getUsername()))
                .findFirst();
        if (existing.isPresent()) {
            throw new RuntimeException("User with username " + user.getUsername() + " already exists.");
        }

        users.add(user);
        writeUsers(users);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> users = readUsers();
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<User> findById(String id) {
        List<User> users = readUsers();
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

}
