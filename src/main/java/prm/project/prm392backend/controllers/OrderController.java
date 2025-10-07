package prm.project.prm392backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.OrderCreateRequest;
import prm.project.prm392backend.dtos.OrderCreateResponse;
import prm.project.prm392backend.pojos.Cart;
import prm.project.prm392backend.pojos.Order;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.CartRepository;
import prm.project.prm392backend.repositories.OrderRepository;
import prm.project.prm392backend.repositories.UserRepository;

import java.util.Date;

@RestController
@RequestMapping("/api/order")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody OrderCreateRequest request){
        User user = userRepository.findUserById(request.getUserId());
        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if(cart!=null){
            Order order = new Order();
            order.setUserID(user);
            order.setCartID(cart);
            order.setBillingAddress(request.getBillingAddress());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setOrderDate(new Date());
            order.setOrderStatus("WAITING PAYMENT");
            orderRepository.save(order);
            OrderCreateResponse response = modelMapper.map(order,OrderCreateResponse.class);
            return ResponseEntity.ok(response);
        }else{
            return ResponseEntity.status(404).body("Not found cart");
        }
    }


}
