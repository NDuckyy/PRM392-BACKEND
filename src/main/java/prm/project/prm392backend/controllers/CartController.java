package prm.project.prm392backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.*;
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

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCurrentCartByUserId(@PathVariable Integer userId){
        ApiResponse<CartResponse> res = new ApiResponse<>();

        User user = userRepository.findUserById(userId);
        if (user == null) {
            res.setCode(404);
            res.setMessage("User not found with ID: " + userId);
            res.setData(null);
            return res;
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        if (cart == null) {
            CartResponse empty = new CartResponse();
            empty.setId(null);
            empty.setTotalPrice(BigDecimal.ZERO);
            empty.setCartItemResponses(List.of());
            res.setCode(200);
            res.setMessage("Cart is empty");
            res.setData(empty);
            return res;
        }

        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
        List<CartItemResponse> cartItemResponses = cartItemList.stream()
                .map(item -> {
                    CartItemResponse r = modelMapper.map(item, CartItemResponse.class);
                    r.setProductId(item.getProductID().getId());
                    r.setProductName(item.getProductID().getProductName());
                    r.setImageURL(item.getProductID().getImageURL());
                    return r;
                })
                .toList();
        cartResponse.setCartItemResponses(cartItemResponses);

        res.setCode(200);
        res.setMessage("Fetched cart successfully");
        res.setData(cartResponse);
        return res;
    }

    @PostMapping("/add")
    public ApiResponse<CartInsertResponse> cartInsert(@RequestBody CartInsertRequest request){
        ApiResponse<CartInsertResponse> res = new ApiResponse<>();

        User user = userRepository.findUserById(request.getUserId());
        if (user == null) {
            res.setCode(404);
            res.setMessage("User not found with ID: " + request.getUserId());
            return res;
        }

        Product product = productRepository.findProductById(request.getProductId());
        if (product == null) {
            res.setCode(404);
            res.setMessage("Product not found with ID: " + request.getProductId());
            return res;
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            res.setCode(400);
            res.setMessage("Quantity must be greater than 0");
            return res;
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        if (cart == null) {
            Cart newCart = new Cart();
            newCart.setUserID(user);
            newCart.setTotalPrice(BigDecimal.ZERO);
            newCart.setStatus("ACTIVE");
            cart = cartRepository.save(newCart);
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

        res.setCode(200);
        res.setMessage("OK");
        res.setData(data);
        return res;
    }

    @PutMapping("/update/{cartItemId}")
    public ApiResponse<CartItemUpdateResponse> cartUpdateQuantity(
            @PathVariable Integer cartItemId,
            @RequestBody CartUpdateRequest request){
        ApiResponse<CartItemUpdateResponse> res = new ApiResponse<>();

        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        if (cartItem == null) {
            res.setCode(404);
            res.setMessage("Cart item not found with ID: " + cartItemId);
            return res;
        }

        if (request.getQuantity() == null || request.getQuantity() < 0) {
            res.setCode(400);
            res.setMessage("Quantity must be >= 0");
            return res;
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

            res.setCode(200);
            res.setMessage("OK");
            res.setData(data);
            return res;
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

            res.setCode(200);
            res.setMessage("OK");
            res.setData(data);
            return res;
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    public ApiResponse<String> cartDelete(@PathVariable Integer cartItemId){
        ApiResponse<String> res = new ApiResponse<>();

        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        if (cartItem == null) {
            res.setCode(404);
            res.setMessage("Cart item not found with ID: " + cartItemId);
            return res;
        }

        Cart cart = cartItem.getCartID();
        BigDecimal oldPrice = cartItem.getPrice();
        cart.setTotalPrice(cart.getTotalPrice().subtract(oldPrice));
        cartRepository.save(cart);
        cartItemRepository.delete(cartItem);

        res.setCode(200);
        res.setMessage("Cart item removed from cart");
        res.setData("Cart item removed from cart");
        return res;
    }

    @DeleteMapping("/clear/{userId}")
    public ApiResponse<String> cartClear(@PathVariable Integer userId){
        ApiResponse<String> res = new ApiResponse<>();

        User user = userRepository.findUserById(userId);
        if (user == null) {
            res.setCode(404);
            res.setMessage("User not found with ID: " + userId);
            return res;
        }

        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        if (cart == null) {
            res.setCode(404);
            res.setMessage("Cart not found for user");
            return res;
        }

        List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        cartItemRepository.deleteAll(cartItemList);

        res.setCode(200);
        res.setMessage("Cart cleared successfully");
        res.setData("Cart cleared successfully");
        return res;
    }
}
