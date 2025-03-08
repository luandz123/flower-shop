package luanlocweb.groud.flower_shop.service;

import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.model.Category;
import luanlocweb.groud.flower_shop.repository.ProductRepository;
import luanlocweb.groud.flower_shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }
    
    // Thêm phương thức để lấy sản phẩm theo danh mục
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return productRepository.findByCategory(category);
    }
    
    // Thêm phương thức để tìm kiếm sản phẩm theo từ khóa
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByNameContaining(keyword);
    }
    
    // Phương thức để lấy dữ liệu báo cáo sản phẩm
    public Map<String, Object> getProductReportData() {
        Map<String, Object> productData = new HashMap<>();
        
        List<Product> allProducts = productRepository.findAll();
        
        // Tổng số sản phẩm
        long totalProducts = allProducts.size();
        
        // Số sản phẩm theo danh mục
        Map<String, Long> productsByCategory = allProducts.stream()
            .filter(product -> product.getCategory() != null)
            .collect(Collectors.groupingBy(
                product -> product.getCategory().getName(),
                Collectors.counting()
            ));
        
        // Sản phẩm hết hàng
        List<Product> outOfStock = allProducts.stream()
            .filter(product -> product.getStockQuantity() <= 0)
            .collect(Collectors.toList());
        
        // Sản phẩm sắp hết hàng (ít hơn 5 sản phẩm)
        List<Product> lowStock = allProducts.stream()
            .filter(product -> product.getStockQuantity() > 0 && product.getStockQuantity() < 5)
            .collect(Collectors.toList());
        
        productData.put("totalProducts", totalProducts);
        productData.put("productsByCategory", productsByCategory);
        productData.put("outOfStock", outOfStock);
        productData.put("lowStock", lowStock);
        
        return productData;
    }
}