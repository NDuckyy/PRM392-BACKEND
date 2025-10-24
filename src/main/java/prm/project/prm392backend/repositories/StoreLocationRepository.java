package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prm.project.prm392backend.pojos.Provider;
import prm.project.prm392backend.pojos.StoreLocation;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface StoreLocationRepository extends JpaRepository<StoreLocation, Integer> {
    Optional<StoreLocation> findByProvider_Id(Integer providerId);
    StoreLocation findByProvider(Provider provider);
}
