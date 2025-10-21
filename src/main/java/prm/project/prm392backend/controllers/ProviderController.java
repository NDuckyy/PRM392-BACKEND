package prm.project.prm392backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.RegisterProviderRequest;
import prm.project.prm392backend.enums.Role;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.ProviderRepository;
import prm.project.prm392backend.repositories.UserRepository;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addProvider(@RequestBody RegisterProviderRequest registerProviderRequest, @RequestHeader(value = "Authorization", required = false) String authHeader) {
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
        Provider existingProvider = providerRepository.findByUser(user);
        if (existingProvider != null) {
            throw new AppException(ErrorCode.PROVIDER_ALREADY_EXISTS);
        }
        Provider provider = new Provider();
        provider.setProviderName(registerProviderRequest.getName());
        provider.setUser(user);
        providerRepository.save(provider);
        user.setRole(Role.PROVIDER);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(null, "Provider registered successfully"));
    }
}
