package org.example.node.kafka;

import org.example.node.model.User;
import org.example.node.repository.UserRepository;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class SignUpEventConsumer {

    public static final String USER_ALREADY_EXISTS_OR_ERROR_OCCURRED = "User already exists or error occurred: ";
    public static final String USER_SIGNUPS = "user-signups";
    public static final String $_SPRING_KAFKA_CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";
    @Autowired
    UserRepository userRepository;

    public SignUpEventConsumer() {}

    @KafkaListener(
            topics = USER_SIGNUPS,
            groupId = $_SPRING_KAFKA_CONSUMER_GROUP_ID
    )
    public void listen(User user) {
        try {
            userRepository.save(user);
            createUserDirectory(user);
        } catch (Exception e) {
            System.out.println(USER_ALREADY_EXISTS_OR_ERROR_OCCURRED + e.getMessage());
        }
    }

    private void createUserDirectory(User newUser) throws IOException {
        Path userDirectoryPath = PathUtil.buildPath(newUser.getUsername() + "_" + newUser.getId());
        DirectoryUtil.createDirectory(userDirectoryPath);
    }
}
