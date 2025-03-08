package luanlocweb.groud.flower_shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.service.ProductService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;
    
    @GetMapping
public String viewCart(HttpSession session, Model model) {
    Map<Long, Integer> cart = getCartFromSession(session);
    Map<Product, Integer> cartItems = new HashMap<>();
    
    BigDecimal total = BigDecimal.ZERO;
    for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
        Product product = productService.getProductById(entry.getKey());
        cartItems.put(product, entry.getValue());
        total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
    }
    
    model.addAttribute("cartItems", cartItems);
    model.addAttribute("total", total);
    return "cart";
}
    
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, 
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
        session.setAttribute("cart", cart);
        session.setAttribute("cartCount", getCartCount(cart));
        return "redirect:/cart";
    }
    
    @PostMapping("/update/{productId}")
    public String updateCartItem(@PathVariable Long productId, 
                                @RequestParam Integer quantity,
                                HttpSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
        session.setAttribute("cart", cart);
        session.setAttribute("cartCount", getCartCount(cart));
        return "redirect:/cart";
    }
    
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);
        session.setAttribute("cartCount", getCartCount(cart));
        return "redirect:/cart";
    }
    
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        session.setAttribute("cartCount", 0);
        return "redirect:/cart";
    }
    
        @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCartFromSession(HttpSession session) {
        Object cartObj = session.getAttribute("cart");
        return (cartObj instanceof Map) ? (Map<Long, Integer>) cartObj : new HashMap<>();
    }
    
    private int getCartCount(Map<Long, Integer> cart) {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }
}
