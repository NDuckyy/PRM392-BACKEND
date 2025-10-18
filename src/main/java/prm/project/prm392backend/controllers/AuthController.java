package prm.project.prm392backend.controllers;

import org.apache.tomcat.Jar;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.AuthResponse;
import prm.project.prm392backend.dtos.LoginRequest;
import prm.project.prm392backend.dtos.RegisterRequest;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        ApiResponse<AuthResponse> response = new ApiResponse<>();
        AuthResponse authResponse = new AuthResponse();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            response.setMessage("Username already exists!");
            response.setCode(400);
            return response;
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.setMessage("Email already exists!");
            response.setCode(400);
            return response;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole() == null ? "User" : request.getRole());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        authResponse.setUsername(user.getUsername());
        authResponse.setRole(user.getRole());
        authResponse.setToken(token);

        response.setMessage("User registered successfully!");
        response.setData(authResponse);
        response.setCode(200);

        return response;
    }


    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response = new ApiResponse<>();
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            response.setMessage("Invalid username or password!");
            response.setCode(400);
            return response;
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername(user.getUsername());
        authResponse.setRole(user.getRole());
        authResponse.setToken(token);
        response.setMessage("Login successful!");
        response.setData(authResponse);
        return response;

    }
}
