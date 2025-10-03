package prm.project.prm392backend.controllers;

import org.apache.tomcat.Jar;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prm.project.prm392backend.configs.JwtUtil;
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


    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        AuthResponse response = new AuthResponse();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            response.setMessage("Username already exists!");
            return response;
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.setMessage("Email already exists!");
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

        response.setMessage("User registered successfully!");
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        AuthResponse response = new AuthResponse();

        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(user -> {
                    String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
                    response.setMessage("Login successful!");
                    response.setUsername(user.getUsername());
                    response.setRole(user.getRole());
                    response.setToken(token);
                    return response;
                })
                .orElseGet(() -> {
                    response.setMessage("Invalid username or password");
                    return response;
                });
    }
}
