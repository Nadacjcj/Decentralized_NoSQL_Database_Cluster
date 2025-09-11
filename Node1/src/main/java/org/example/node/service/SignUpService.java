package org.example.node.service;

import org.example.node.dto.SignUpRequest;
import org.example.node.dto.SignUpResponse;
import org.example.node.model.User;
import org.example.node.repository.JsonRepository;
import org.example.node.repository.UserRepository;
import org.example.node.util.ConsistentHashing;
import org.example.node.util.DirectoryUtil;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class SignUpService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    ConsulServiceDiscovery consulServiceDiscovery;

    @Autowired
    private JsonRepository jsonRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;


    public SignUpService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public SignUpResponse signUp(SignUpRequest signUpRequest) throws IOException {
        userRepository.findByUsername(signUpRequest.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("Username already exists"); });

        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User newUser = new User(signUpRequest.getUsername(), hashedPassword);

        List<String> nodes = consulServiceDiscovery.getAllServiceNodes();
        ConsistentHashing ch = new ConsistentHashing(nodes.toArray(new String[0]), 100);
        String affinityNode = ch.getNode(newUser.getId());
        newUser.setAffinityNode(affinityNode);

        userRepository.save(newUser);
        createUserDirectory(newUser);

        kafkaTemplate.send("user-signups", newUser);

        return new SignUpResponse(
                newUser.getId(),
                "User registered successfully on this node: " + " (affinity: " + affinityNode + ")"
        );
    }

    private void createUserDirectory(User newUser) throws IOException {
        Path userDirectoryPath = PathUtil.buildPath(newUser.getUsername() + "_" + newUser.getId());
        DirectoryUtil.createDirectory(userDirectoryPath);
    }
}
