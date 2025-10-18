package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.enums.Role;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.UserRepository;
import prm.project.prm392backend.configs.JwtUtil;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ApiResponse<UserDTO> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        ApiResponse<UserDTO> res = new ApiResponse<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setCode(401);
            res.setMessage("Missing Authorization: Bearer <token>");
            res.setData(null);
            return res;
        }
        String token = authHeader.substring(7).trim();

        // Xác thực chữ ký + hạn token
        if (!JwtUtil.validateToken(token)) {
            res.setCode(401);
            res.setMessage("Invalid or expired token");
            res.setData(null);
            return res;
        }

        // Lấy userId từ claim
        Integer userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            res.setCode(401);
            res.setMessage("Token has no userId");
            res.setData(null);
            return res;
        }

        return userRepository.findById(userId)
                .map(u -> {
                    res.setCode(200);
                    res.setMessage("Fetched profile successfully");
                    res.setData(new UserDTO(u));
                    return res;
                })
                .orElseGet(() -> {
                    res.setCode(404);
                    res.setMessage("User not found with id = " + userId);
                    res.setData(null);
                    return res;
                });
    }

    // DTO che passwordHash
    static class UserDTO {
        public Integer id;
        public String username;
        public String email;
        public String phoneNumber;
        public String address;
        public Role role;

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
