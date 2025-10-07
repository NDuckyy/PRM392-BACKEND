package prm.project.prm392backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin("*")
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
    public ResponseEntity<?> getCurrentCartByUserId(@PathVariable Integer userId){
        User user = userRepository.findUserById(userId);
        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        CartResponse cartResponse = modelMapper.map(cart, CartResponse.class);
        List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
        List<CartItemResponse> cartItemResponses = cartItemList.stream()
                .map(item -> {
                    CartItemResponse response = modelMapper.map(item, CartItemResponse.class);
                    response.setProductId(item.getProductID().getId());
                    response.setProductName(item.getProductID().getProductName());
                    response.setImageURL(item.getProductID().getImageURL());
                    return response;
                })
                .toList();
        cartResponse.setCartItemResponses(cartItemResponses);
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<?> cartInsert(@RequestBody CartInsertRequest request){
        User user = userRepository.findUserById(request.getUserId());
        Product product = productRepository.findProductById(request.getProductId());
        if (product == null) {
            return ResponseEntity.status(404).body("Product not found with ID: " + request.getProductId());
        }
        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        if(cart == null){
            Cart newCart = new Cart();
            newCart.setUserID(user);
            newCart.setTotalPrice(BigDecimal.ZERO);
            newCart.setStatus("ACTIVE");
            cart = cartRepository.save(newCart);
        }
        CartItem cartItem = cartItemRepository.findCartItemByCartIDAndProductID(cart,product);
        if(cartItem!=null){

            Integer oldQuantityProduct = cartItem.getQuantity();
            Integer newQuantityProduct = oldQuantityProduct+request.getQuantity();
            cartItem.setQuantity(newQuantityProduct);

            BigDecimal oldPrice = product.getPrice().multiply(BigDecimal.valueOf(oldQuantityProduct));
            BigDecimal newPrice = product.getPrice().multiply(BigDecimal.valueOf(newQuantityProduct));
            cartItem.setPrice(newPrice);

            BigDecimal totalPrice = cartItem.getCartID().getTotalPrice().subtract(oldPrice).add(newPrice);
            cartItem.getCartID().setTotalPrice(totalPrice);
            cartItemRepository.save(cartItem);
            CartInsertResponse response = new CartInsertResponse();
            response.setMessage("Product add cart successfully");
            response.setCartId(cart.getId());
            response.setNewTotal(totalPrice);
            return ResponseEntity.ok(response);
        }else{
            CartItem newCartItem = new CartItem();
            newCartItem.setCartID(cart);
            newCartItem.setProductID(product);
            newCartItem.setQuantity(request.getQuantity());
            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            newCartItem.setPrice(price);
            cartItemRepository.save(newCartItem);
            BigDecimal totalPrice = cart.getTotalPrice().add(price);
            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);
            CartInsertResponse response = new CartInsertResponse();
            response.setMessage("Product add cart successfully");
            response.setCartId(cart.getId());
            response.setNewTotal(totalPrice);
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> cartUpdateQuantity(@PathVariable Integer cartItemId, CartUpdateRequest request){
        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        Cart cart = cartItem.getCartID();
        if(request.getQuantity()==0){
            BigDecimal oldPrice = cartItem.getPrice();
            cart.setTotalPrice(cart.getTotalPrice().subtract(oldPrice));
            cartRepository.save(cart);
            cartItemRepository.delete(cartItem);
            return ResponseEntity.ok("Cart item removed from cart");
        }else{
            Integer oldQuantity = cartItem.getQuantity();
            BigDecimal oldPrice = cartItem.getProductID().getPrice().multiply(BigDecimal.valueOf(oldQuantity));
            cartItem.setQuantity(request.getQuantity());
            BigDecimal newPrice = cartItem.getProductID().getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            cartItem.setPrice(newPrice);
            BigDecimal totalPrice = cartItem.getCartID().getTotalPrice().subtract(oldPrice).add(newPrice);
            cartItem.getCartID().setTotalPrice(totalPrice);
            cartItemRepository.save(cartItem);
            CartItemUpdateResponse response = new CartItemUpdateResponse();
            response.setMessage("Cart item quantity updated");
            response.setCartId(cart.getId());
            response.setTotalPrice(totalPrice);
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<?> cartDelete(@PathVariable Integer cartItemId){
        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        Cart cart = cartItem.getCartID();
        BigDecimal oldPrice = cartItem.getPrice();
        cart.setTotalPrice(cart.getTotalPrice().subtract(oldPrice));
        cartRepository.save(cart);
        cartItemRepository.delete(cartItem);
        return ResponseEntity.ok("Cart item removed from cart");
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> cartClear(@PathVariable Integer userId){
        User user = userRepository.findUserById(userId);
        Cart cart = cartRepository.findCartByUserIDAndStatus(user,"ACTIVE");
        if(cart!=null){
            List<CartItem> cartItemList = cartItemRepository.findCartItemByCartID(cart);
            cart.setTotalPrice(BigDecimal.ZERO);
            cartRepository.save(cart);
            cartItemRepository.deleteAll(cartItemList);
            return ResponseEntity.ok("Cart cleared successfully");
        }
        return ResponseEntity.status(404).body("Cant found cart");
    }


}
