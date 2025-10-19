package prm.project.prm392backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prm.project.prm392backend.pojos.Category;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
