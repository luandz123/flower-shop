package luanlocweb.groud.flower_shop.controller;

import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "product-list";
    }

    @GetMapping("/{id}")
    public String getProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }
    
    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(@PathVariable Long categoryId, Model model) {
        model.addAttribute("products", productService.getProductsByCategory(categoryId));
        model.addAttribute("categoryId", categoryId);
        return "product-list";
    }
    
    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        model.addAttribute("products", productService.searchProducts(keyword));
        model.addAttribute("keyword", keyword);
        return "product-list";
    }
}
