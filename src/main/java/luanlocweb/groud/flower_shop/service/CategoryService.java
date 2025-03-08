package luanlocweb.groud.flower_shop.service;

import org.springframework.stereotype.Service;

import luanlocweb.groud.flower_shop.model.Category;
import luanlocweb.groud.flower_shop.repository.CategoryRepository;

import java.util.List;
import java.util.ArrayList;

@Service
public class CategoryService {
    private CategoryRepository categoryRepository;
    // Returns a list of all categories.
    public List<Category> getAllCategories() {
        // In a real application, you'd retrieve categories from a repository or database.
        return new ArrayList<>();
    }

    // Saves a new category.
    public void saveCategory(Category category) {
        // Implement saving logic here.
    }

    // Deletes a category by its ID.
    public void deleteCategory(Long id) {
        // Implement deletion logic here.
    }
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

}