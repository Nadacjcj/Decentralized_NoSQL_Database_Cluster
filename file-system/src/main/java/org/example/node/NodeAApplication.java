package org.example.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
public class NodeAApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeAApplication.class, args);
    }

}
