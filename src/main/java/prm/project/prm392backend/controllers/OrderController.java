package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.OrderCreateRequest;
import prm.project.prm392backend.dtos.OrderCreateResponse;
import prm.project.prm392backend.dtos.OrderResponse;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.Cart;
import prm.project.prm392backend.pojos.Order;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.CartRepository;
import prm.project.prm392backend.repositories.OrderRepository;
import prm.project.prm392backend.repositories.UserRepository;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<OrderCreateResponse>> create(@RequestBody OrderCreateRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new AppException(ErrorCode.MISSING_PARAMETER);
        }

        User user = userRepository.findUserById(request.getUserId());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if (cart == null) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }

        try {
            Order order = new Order();
            order.setUserID(user);
            order.setCartID(cart);
            order.setBillingAddress(request.getBillingAddress());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setOrderDate(new Date());
            order.setOrderStatus("WAITING PAYMENT");

            orderRepository.save(order);

            OrderCreateResponse data = modelMapper.map(order, OrderCreateResponse.class);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(data, "Order created successfully"));
        } catch (Exception e) {
            throw new AppException(ErrorCode.ORDER_CREATE_FAILED);
        }
    }

    @GetMapping
    @Operation(summary = "Get all orders by user ID")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(
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

        User user = userRepository.findUserById(userId);
        // Tuỳ business: nếu token hợp lệ nhưng user đã bị xoá → 404
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        List<Order> orders = orderRepository.findOrdersByUserID(user);
        List<OrderResponse> orderResponses = orders.stream()
                .map(o -> modelMapper.map(o, OrderResponse.class))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(orderResponses, "Fetched orders successfully"));
    }
}
