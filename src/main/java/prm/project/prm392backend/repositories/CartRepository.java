package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Cart;
import prm.project.prm392backend.pojos.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findCartByUserIDAndStatus(User userId, String status);

    Cart findCartByUserID(User userId);
}
