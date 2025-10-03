package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prm.project.prm392backend.pojos.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
