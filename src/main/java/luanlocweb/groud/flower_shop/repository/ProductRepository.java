package luanlocweb.groud.flower_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.model.Category;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByNameContaining(String keyword);
    
    List<Product> findByCategoryAndNameContaining(Category category, String keyword);
    
    List<Product> findByFeaturedTrue();
    
    List<Product> findByStockQuantityLessThanEqual(int quantity);
    
    List<Product> findTop10ByOrderBySalesCountDesc();
}