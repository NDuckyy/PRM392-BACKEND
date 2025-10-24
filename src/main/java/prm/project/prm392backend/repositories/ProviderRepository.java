package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.User;


@Repository
public interface ProviderRepository extends JpaRepository<Provider,Integer> {
    Provider findByUser(User user);

}
