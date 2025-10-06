package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    long countByCartID_UserID_Id(Integer userId);
}
