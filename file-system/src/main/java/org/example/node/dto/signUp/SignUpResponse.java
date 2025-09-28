package org.example.node.dto.signUp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpResponse {
    private String userId;
    private String message;

    public SignUpResponse(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }
}
