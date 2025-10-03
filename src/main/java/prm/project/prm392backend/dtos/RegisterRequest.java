package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String role;
}