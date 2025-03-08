package luanlocweb.groud.flower_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import luanlocweb.groud.flower_shop.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Category findByName(String name);
    
    boolean existsByName(String name);
}