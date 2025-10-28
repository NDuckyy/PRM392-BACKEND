package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Order;
import prm.project.prm392backend.pojos.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findAllByOrderIn(List<Order> orders);
}
