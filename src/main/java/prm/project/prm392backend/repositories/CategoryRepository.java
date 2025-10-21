package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prm.project.prm392backend.pojos.Category;
@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
