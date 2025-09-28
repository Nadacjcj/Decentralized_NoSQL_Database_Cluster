package com.bootstrap.bootstrap.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BootstrappingService {

    public String launchSystem() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker-compose", "up", "--build"
            );
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index";
    }
    public String shutdownSystem() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker-compose", "down"
            );
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index";
    }
}
