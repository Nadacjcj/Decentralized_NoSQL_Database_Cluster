package org.example.node.service.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.node.dto.signUp.SignUpRequest;
import org.example.node.dto.signUp.SignUpResponse;
import org.example.node.locks.ConsulLockService;
import org.example.node.model.User;
import org.example.node.repository.JsonRepository;
import org.example.node.repository.UserRepository;
import org.example.node.service.consul.ConsulServiceDiscovery;
import org.example.node.util.loginutil.ConsistentHashing;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class SignUpService {

    private static final Logger logger = LogManager.getLogger(SignUpService.class);

    public static final String USER_SIGNUPS = "user-signups";
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    private ConsulServiceDiscovery consulServiceDiscovery;
    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private ConsulLockService lockService;

    public SignUpService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public SignUpResponse signUp(SignUpRequest signUpRequest) throws IOException {
        String lockKey = "user-signup:" + signUpRequest.getUsername();
        logger.info("Attempting signup for user: {}", signUpRequest.getUsername());

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired) {
            logger.warn("Could not acquire lock for user signup: {}", signUpRequest.getUsername());
            return new SignUpResponse(null, "Could not acquire lock for user signup, try again later");
        }

        try {
            if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
                logger.warn("User already exists: {}", signUpRequest.getUsername());
                return new SignUpResponse(null, "User already exists");
            }

            String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
            User newUser = new User(signUpRequest.getUsername(), hashedPassword);
            logger.debug("Created new user object with ID: {}", newUser.getId());

            List<String> nodes = consulServiceDiscovery.getAllServiceNodes();
            ConsistentHashing ch = new ConsistentHashing(nodes.toArray(new String[0]), 100);
            String affinityNode = ch.getNode(newUser.getId());
            newUser.setAffinityNode(affinityNode);
            logger.info("Assigned user {} to affinity node: {}", newUser.getUsername(), affinityNode);

            userRepository.save(newUser);
            createUserDirectory(newUser);
            kafkaTemplate.send(USER_SIGNUPS, newUser);
            logger.info("User signup successful: {}", newUser.getUsername());

            return new SignUpResponse(
                    newUser.getId(),
                    "User registered successfully on this node (affinity: " + affinityNode + ")"
            );
        } finally {
            lockService.releaseWriteLock(lockKey);
            logger.debug("Released signup lock for user: {}", signUpRequest.getUsername());
        }
    }

    private void createUserDirectory(User newUser) throws IOException {
        Path userDirectoryPath = PathUtil.buildPath(newUser.getUsername() + "_" + newUser.getId());
        DirectoryUtil.createDirectory(userDirectoryPath);
        logger.debug("Created directory for user: {}", userDirectoryPath);
    }
}
