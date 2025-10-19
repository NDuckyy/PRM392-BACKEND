package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.repositories.CartItemRepository;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemRepository cartItemRepository;

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countCartItems(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader) {
        Integer userId = JwtUtil.extractUserId(authHeader);
        if (userId == null) {
            throw new AppException(ErrorCode.MISSING_PARAMETER);
        }

        long count = cartItemRepository.countByCartID_UserID_Id(userId);

        return ResponseEntity.ok(ApiResponse.ok(count, "Count cart items successfully"));
    }
}
