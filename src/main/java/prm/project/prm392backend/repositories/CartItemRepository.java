package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Cart;
import prm.project.prm392backend.pojos.CartItem;
import prm.project.prm392backend.pojos.Product;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findCartItemByCartID(Cart cart);

    CartItem findCartItemByCartIDAndProductID(Cart cartId, Product productId);

    CartItem findCartItemById(Integer cartItemId);

    long countByCartID_UserID_Id(Integer userId);

}
