package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.OrderCreateRequest;
import prm.project.prm392backend.dtos.OrderCreateResponse;
import prm.project.prm392backend.dtos.OrderResponse;
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
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @PostMapping
    public ApiResponse<OrderCreateResponse> create(@RequestBody OrderCreateRequest request) {
        ApiResponse<OrderCreateResponse> res = new ApiResponse<>();

        if (request == null || request.getUserId() == null) {
            res.setCode(400);
            res.setMessage("userId is required");
            res.setData(null);
            return res;
        }

        User user = userRepository.findUserById(request.getUserId());
        if (user == null) {
            res.setCode(404);
            res.setMessage("User not found with id = " + request.getUserId());
            res.setData(null);
            return res;
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if (cart == null) {
            res.setCode(404);
            res.setMessage("Active cart not found for user");
            res.setData(null);
            return res;
        }

        Order order = new Order();
        order.setUserID(user);
        order.setCartID(cart);
        order.setBillingAddress(request.getBillingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderDate(new Date());
        order.setOrderStatus("WAITING PAYMENT");

        orderRepository.save(order);

        OrderCreateResponse data = modelMapper.map(order, OrderCreateResponse.class);

        res.setCode(201); // Created
        res.setMessage("Order created successfully");
        res.setData(data);
        return res;
    }


    @GetMapping
    @Operation(summary = "Get all orders by user ID")
    public ApiResponse<List<OrderResponse>> getOrdersByUserId(@RequestHeader (value = "Authorization", required = false) String authHeader) {
        ApiResponse<List<OrderResponse>> res = new ApiResponse<>();

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
        User user = userRepository.findUserById(userId);
        List<Order> orders = orderRepository.findOrdersByUserID(user);
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .toList();

        res.setCode(200);
        res.setMessage("Fetched orders successfully");
        res.setData(orderResponses);
        return res;
    }
}
