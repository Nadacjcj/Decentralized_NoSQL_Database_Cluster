package com.bootstrap.bootstrap.controller;

import com.bootstrap.bootstrap.service.BootstrappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;

@Controller
public class BootstrapController {

    @Autowired BootstrappingService bootstrappingService;
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/launch")
    public String launchSystem() {
        return bootstrappingService.launchSystem();
    }

    @PostMapping("/shutdown")
    public String shutdownSystem() {
      return bootstrappingService.shutdownSystem();
    }
}
