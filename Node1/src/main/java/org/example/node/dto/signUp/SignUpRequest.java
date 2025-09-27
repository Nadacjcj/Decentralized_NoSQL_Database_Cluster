package org.example.node.dto.signUp;

import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpRequest {

    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    private String username;

    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
