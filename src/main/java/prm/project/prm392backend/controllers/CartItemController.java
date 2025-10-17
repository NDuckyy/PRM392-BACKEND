package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.repositories.CartItemRepository;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemRepository cartItemRepository;

    @GetMapping("/count")
    public ResponseEntity<Long> countCartItems(@Parameter(hidden = true)
                                                   @RequestHeader(name = "Authorization", required = false) String token) {
        Integer userId = JwtUtil.extractUserId(token);
        long count = cartItemRepository.countByCartID_UserID_Id(userId);
        return ResponseEntity.ok(count);
    }
}
