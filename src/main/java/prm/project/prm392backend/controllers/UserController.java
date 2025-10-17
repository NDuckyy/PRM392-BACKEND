package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.UserRepository;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing Authorization: Bearer <token>");
        }
        String token = authHeader.substring(7).trim();

        // Xác thực chữ ký + hạn token
        if (!JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        // Lấy userId từ claim
        Integer userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body("Token has no userId");
        }

        // Trả về profile (ẩn passwordHash)
        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(new UserDTO(u)))
                .orElseGet(() -> ResponseEntity.status(404).build());


    }

    // DTO che passwordHash
    static class UserDTO {
        public Integer id;
        public String username;
        public String email;
        public String phoneNumber;
        public String address;
        public String role;

        UserDTO(User u) {
            this.id = u.getId();
            this.username = u.getUsername();
            this.email = u.getEmail();
            this.phoneNumber = u.getPhoneNumber();
            this.address = u.getAddress();
            this.role = u.getRole();
        }
    }
}
