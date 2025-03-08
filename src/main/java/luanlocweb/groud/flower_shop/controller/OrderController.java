package luanlocweb.groud.flower_shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import luanlocweb.groud.flower_shop.model.Order;
import luanlocweb.groud.flower_shop.model.Product;

import luanlocweb.groud.flower_shop.model.User;
import luanlocweb.groud.flower_shop.service.OrderService;
import luanlocweb.groud.flower_shop.service.ProductService;
import luanlocweb.groud.flower_shop.service.UserService;


import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String viewOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "orders";
    }
    
    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        Order order = orderService.getOrderById(id);
        
        // Kiểm tra bảo mật: chỉ cho phép user xem đơn của mình
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        return "order-details";
    }
    
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        // Kiểm tra giỏ hàng
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Lấy thông tin user hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        model.addAttribute("user", user);
        
        // Tính tổng tiền
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        
        model.addAttribute("total", total);
        model.addAttribute("cartCount", session.getAttribute("cartCount"));
        return "checkout";
    }
    
    @PostMapping("/place")
    public String placeOrder(@RequestParam String shippingAddress, 
                           @RequestParam String paymentMethod,
                           @RequestParam(required = false) String notes,
                           HttpSession session) {
        
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        
        Order order = orderService.createOrder(user, cart, shippingAddress, paymentMethod, notes);
        
        // Xóa giỏ hàng sau khi đặt hàng
        session.removeAttribute("cart");
        session.setAttribute("cartCount", 0);
        
        return "redirect:/orders/" + order.getId() + "/confirmation";
    }
    
    @GetMapping("/{id}/confirmation")
    public String orderConfirmation(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "order-confirm";
    }
}