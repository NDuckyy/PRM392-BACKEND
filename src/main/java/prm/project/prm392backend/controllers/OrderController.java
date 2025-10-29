package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.configs.JwtUtil;
import prm.project.prm392backend.dtos.*;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.*;
import prm.project.prm392backend.repositories.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<OrderCreateResponse>> create(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,
            @RequestBody OrderCreateRequest request) {
        Integer userId = JwtUtil.extractUserId(authHeader);
        if (request == null || userId == null) {
            throw new AppException(ErrorCode.MISSING_PARAMETER);
        }

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if (cart == null) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }
        List<CartItem> cartItem = cartItemRepository.findCartItemByCartID(cart);

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
            data.setTotalPrice(cart.getTotalPrice());

            cartItem.forEach(item -> {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setProductName(item.getProductID().getProductName());
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setProductPrice(item.getProductID().getPrice());
                orderDetailRepository.save(orderDetail);
            });

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
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        List<Order> orders = orderRepository.findOrdersByUserID(user);
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderIn(orders);

        // 🔹 Sắp xếp: đơn mới nhất lên đầu
        orders.sort(Comparator.comparing(Order::getOrderDate).reversed());

        Map<Integer, List<OrderDetail>> detailsByOrderId = orderDetails.stream()
                .collect(Collectors.groupingBy(od -> od.getOrder().getId()));

        List<OrderResponse> orderResponses = orders.stream().map(order -> {
            OrderResponse dto = new OrderResponse();
            dto.setPaymentMethod(order.getPaymentMethod());
            dto.setBillingAddress(order.getBillingAddress());
            dto.setOrderStatus(order.getOrderStatus());
            dto.setOrderDate(order.getOrderDate());

            User u = order.getUserID();
            if (u != null) {
                UserResponse ur = new UserResponse();
                ur.setUsername(u.getUsername());
                ur.setEmail(u.getEmail());
                ur.setAddress(u.getAddress());
                ur.setPhoneNumber(u.getPhoneNumber());
                ur.setRole(u.getRole());
                dto.setUserID(ur);
            }

            List<OrderDetailResponse> detailDtos = detailsByOrderId
                    .getOrDefault(order.getId(), List.of())
                    .stream()
                    .map(od -> {
                        OrderDetailResponse d = new OrderDetailResponse();
                        d.setId(od.getId());
                        d.setQuantity(od.getQuantity());
                        d.setUnitPrice(od.getProductPrice());
                        d.setProductId(od.getId());
                        d.setProductName(od.getProductName());
                        return d;
                    }).toList();

            dto.setOrderDetails(detailDtos);
            return dto;
        }).toList();

        return ResponseEntity.ok(ApiResponse.ok(orderResponses, "Fetched orders successfully"));
    }

}
