package org.example.node.kafka;

import org.example.node.dto.SignUpRequest;
import org.example.node.model.User;
import org.example.node.repository.JsonRepository;
import org.example.node.repository.UserRepository;
import org.example.node.service.SignUpService;
import org.example.node.util.DirectoryUtil;
import org.example.node.util.JsonFlattener;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class SignUpEventConsumer {

    @Autowired
    UserRepository userRepository;

    public SignUpEventConsumer() {
    }

    @KafkaListener(
            topics = "user-signups",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(User user) {
        try {
            userRepository.save(user);
            createUserDirectory(user);
        } catch (Exception e) {
            System.out.println("User already exists or error occurred: " + e.getMessage());
        }
    }

    private void createUserDirectory(User newUser) throws IOException {
        Path userDirectoryPath = PathUtil.buildPath(newUser.getUsername() + "_" + newUser.getId());
        DirectoryUtil.createDirectory(userDirectoryPath);
    }
}
