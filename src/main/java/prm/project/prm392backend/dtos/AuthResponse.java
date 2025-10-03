package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class AuthResponse {
    private String message;
    private String token;
    private String username;
    private String role;
}