package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.enums.Role;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.UserRepository;
import prm.project.prm392backend.configs.JwtUtil;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.AUTH_MISSING);
        }
        String token = authHeader.substring(7).trim();

        if (!JwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.AUTH_INVALID);
        }

        Integer userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            throw new AppException(ErrorCode.TOKEN_NO_USERID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.ok(new UserDTO(user), "Fetched profile successfully"));
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
