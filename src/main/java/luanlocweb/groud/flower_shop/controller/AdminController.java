package luanlocweb.groud.flower_shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import luanlocweb.groud.flower_shop.model.Category;
import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.model.User;
import luanlocweb.groud.flower_shop.model.Order;
import luanlocweb.groud.flower_shop.service.CategoryService;
import luanlocweb.groud.flower_shop.service.OrderService;
import luanlocweb.groud.flower_shop.service.ProductService;
import luanlocweb.groud.flower_shop.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String adminDashboard(Model model) {
        // Đếm số lượng sản phẩm, đơn hàng, người dùng
        model.addAttribute("productCount", productService.getAllProducts().size());
        model.addAttribute("orderCount", orderService.getAllOrders().size());
        model.addAttribute("userCount", userService.getAllUsers().size());
        
        // Lấy danh sách đơn hàng đang chờ xử lý
        model.addAttribute("pendingOrders", orderService.getOrdersByStatus("PENDING"));
        
        return "admin-dashboard";
    }
    
    // Quản lý sản phẩm
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }
    
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product,
                           @RequestParam("productImage") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        if (!file.isEmpty()) {
            try {
                // Tạo tên file ngẫu nhiên để tránh trùng lặp
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads/products");
                
                // Tạo thư mục nếu không tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Lưu file
                Files.write(uploadPath.resolve(fileName), file.getBytes());
                
                // Lưu đường dẫn đến file vào sản phẩm
                product.setImageUrl("/uploads/products/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to upload image: " + e.getMessage());
                return "redirect:/admin/products/add";
            }
        }
        
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }
    
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                              @ModelAttribute Product product,
                              @RequestParam("productImage") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        Product existingProduct = productService.getProductById(id);
        
        // Cập nhật thông tin sản phẩm
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setFeatured(product.isFeatured());
        
        if (!file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads/products");
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Files.write(uploadPath.resolve(fileName), file.getBytes());
                existingProduct.setImageUrl("/uploads/products/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to upload image: " + e.getMessage());
            }
        }
        
        productService.saveProduct(existingProduct);
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/admin/products";
    }
    
    // Quản lý danh mục
    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "admin/categories";
    }
    
    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/category-form";
    }
    
    @PostMapping("/categories/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Cannot delete category because it contains products!");
        }
        return "redirect:/admin/categories";
    }
    
    // Quản lý đơn hàng
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }
    
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "admin/order-details";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
        return "redirect:/admin/orders/" + id;
    }
    
    // Quản lý người dùng
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        // Giả sử có phương thức getUserById trong UserService
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.getOrdersByUser(id));
        return "admin/user-details";
    }
    
    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        user.setRole(role);
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User role updated successfully!");
        return "redirect:/admin/users/" + id;
    }
    
    @PostMapping("/users/{id}/status")
    public String updateUserStatus(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        user.setActive(active);
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");
        return "redirect:/admin/users/" + id;
    }
    
    // Thống kê và báo cáo
    @GetMapping("/reports/sales")
    public String showSalesReport(Model model) {
        // Giả sử có service để lấy dữ liệu báo cáo
        model.addAttribute("salesData", orderService.getSalesData());
        return "admin/sales-report";
    }
    
    @GetMapping("/reports/products")
    public String showProductsReport(Model model) {
        // Giả sử có service để lấy dữ liệu báo cáo sản phẩm
        model.addAttribute("productData", productService.getProductReportData());
        return "admin/products-report";
    }
}