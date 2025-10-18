package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.repositories.CartItemRepository;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemRepository cartItemRepository;

    @GetMapping("/count")
    public ApiResponse<Long> countCartItems(@RequestParam Integer userId) {
        ApiResponse<Long> res = new ApiResponse<>();

        if (userId == null) {
            res.setCode(400);
            res.setMessage("UserId is required");
            res.setData(null);
            return res;
        }

        long count = cartItemRepository.countByCartID_UserID_Id(userId);

        res.setCode(200);
        res.setMessage("Count cart items successfully");
        res.setData(count);
        return res;
    }
}
