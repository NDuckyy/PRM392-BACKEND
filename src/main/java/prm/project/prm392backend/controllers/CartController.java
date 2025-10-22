package prm.project.prm392backend.controllers;

import io.swagger.v3.oas.annotations.Parameter;
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
import prm.project.prm392backend.pojos.Cart;
import prm.project.prm392backend.pojos.CartItem;
import prm.project.prm392backend.pojos.Product;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.CartItemRepository;
import prm.project.prm392backend.repositories.CartRepository;
import prm.project.prm392backend.repositories.ProductRepository;
import prm.project.prm392backend.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ModelMapper modelMapper;


    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCartByUserId(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader)
    {
        Integer userId = JwtUtil.extractUserId(authHeader);
        User user = userRepository.findUserById(userId);
        if (user == null) throw new AppException(ErrorCode.USER_NOT_FOUND);

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        CartResponse body = new CartResponse();

        if (cart == null) {
            // Giỏ trống → vẫn 200, trả empty payload
            body.setId(null);
            body.setTotalPrice(BigDecimal.ZERO);
            body.setCartItemResponses(List.of());
            return ResponseEntity.ok(ApiResponse.ok(body, "Cart is empty"));
        }

        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
        List<CartItemResponse> cartItemResponses = cartItemList.stream().map(item -> {
            CartItemResponse r = modelMapper.map(item, CartItemResponse.class);
            r.setProductId(item.getProductID().getId());
            r.setProductName(item.getProductID().getProductName());
            r.setImageURL(item.getProductID().getImageURL());
            return r;
        }).toList();
        cartResponse.setCartItemResponses(cartItemResponses);

        return ResponseEntity.ok(ApiResponse.ok(cartResponse, "Fetched cart successfully"));
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<ApiResponse<CartInsertResponse>> cartInsert(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CartInsertRequest request) {
        Integer userId = JwtUtil.extractUserId(authHeader);
        User user = userRepository.findUserById(userId);
        if (user == null) throw new AppException(ErrorCode.USER_NOT_FOUND);

        Product product = productRepository.findProductById(request.getProductId());
        if (product == null) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.QUANTITY_INVALID);
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if (cart == null) {
            cart = new Cart();
            cart.setUserID(user);
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setStatus("ACTIVE");
            cart = cartRepository.save(cart);
        }

        CartItem cartItem = cartItemRepository.findCartItemByCartIDAndProductID(cart, product);
        BigDecimal totalPrice;

        if (cartItem != null) {
            int newQty = cartItem.getQuantity() + request.getQuantity();
            BigDecimal oldPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            BigDecimal newPrice = product.getPrice().multiply(BigDecimal.valueOf(newQty));

            cartItem.setQuantity(newQty);
            cartItem.setPrice(newPrice);

            totalPrice = cart.getTotalPrice().subtract(oldPrice).add(newPrice);
            cart.setTotalPrice(totalPrice);

            cartItemRepository.save(cartItem);
            cartRepository.save(cart);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCartID(cart);
            newCartItem.setProductID(product);
            newCartItem.setQuantity(request.getQuantity());

            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            newCartItem.setPrice(price);

            cartItemRepository.save(newCartItem);

            totalPrice = cart.getTotalPrice().add(price);
            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);
        }

        CartInsertResponse data = new CartInsertResponse();
        data.setMessage("Product added to cart successfully");
        data.setCartId(cart.getId());
        data.setNewTotal(totalPrice);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(data, "OK"));
    }

    @PutMapping("/update/{cartItemId}")
    @Transactional
    public ResponseEntity<ApiResponse<CartItemUpdateResponse>> cartUpdateQuantity(
            @PathVariable Integer cartItemId,
            @RequestBody CartUpdateRequest request) {

        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        if (cartItem == null) throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);

        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new AppException(ErrorCode.QUANTITY_INVALID);
        }

        Cart cart = cartItem.getCartID();

        if (request.getQuantity() == 0) {
            BigDecimal oldPrice = cartItem.getPrice();
            cart.setTotalPrice(cart.getTotalPrice().subtract(oldPrice));
            cartRepository.save(cart);
            cartItemRepository.delete(cartItem);

            CartItemUpdateResponse data = new CartItemUpdateResponse();
            data.setMessage("Cart item removed from cart");
            data.setCartId(cart.getId());
            data.setTotalPrice(cart.getTotalPrice());

            return ResponseEntity.ok(ApiResponse.ok(data, "OK"));
        } else {
            int oldQty = cartItem.getQuantity();
            BigDecimal oldPrice = cartItem.getProductID().getPrice().multiply(BigDecimal.valueOf(oldQty));

            cartItem.setQuantity(request.getQuantity());
            BigDecimal newPrice = cartItem.getProductID().getPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));
            cartItem.setPrice(newPrice);

            BigDecimal totalPrice = cart.getTotalPrice().subtract(oldPrice).add(newPrice);
            cart.setTotalPrice(totalPrice);

            cartItemRepository.save(cartItem);
            cartRepository.save(cart);

            CartItemUpdateResponse data = new CartItemUpdateResponse();
            data.setMessage("Cart item quantity updated");
            data.setCartId(cart.getId());
            data.setTotalPrice(totalPrice);

            return ResponseEntity.ok(ApiResponse.ok(data, "OK"));
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> cartDelete(@PathVariable Integer cartItemId) {
        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        if (cartItem == null) throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);

        Cart cart = cartItem.getCartID();
        BigDecimal oldPrice = cartItem.getPrice();
        cart.setTotalPrice(cart.getTotalPrice().subtract(oldPrice));
        cartRepository.save(cart);
        cartItemRepository.delete(cartItem);

        return ResponseEntity.ok(ApiResponse.ok("Cart item removed from cart", "Cart item removed from cart"));
    }

    @DeleteMapping("/clear/current-user")
    @Transactional
    public ResponseEntity<ApiResponse<String>> cartClear(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader) {
        Integer userId = JwtUtil.extractUserId(authHeader);
        User user = userRepository.findUserById(userId);
        if (user == null) throw new AppException(ErrorCode.USER_NOT_FOUND);

        Cart cart = cartRepository.findCartByUserIDAndStatus(user, "ACTIVE");
        if (cart == null) throw new AppException(ErrorCode.CART_NOT_FOUND);

        List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        cartItemRepository.deleteAll(cartItemList);

        return ResponseEntity.ok(ApiResponse.ok("Cart cleared successfully", "Cart cleared successfully"));
    }
}
