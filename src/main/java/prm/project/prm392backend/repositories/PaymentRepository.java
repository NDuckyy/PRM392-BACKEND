package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {
}
