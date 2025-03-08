package luanlocweb.groud.flower_shop.controller;

import luanlocweb.groud.flower_shop.model.Order;
import luanlocweb.groud.flower_shop.service.OrderService;
import luanlocweb.groud.flower_shop.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/process/{orderId}")
    public String showPaymentForm(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "payment-form";
    }
    
    @PostMapping("/process/{orderId}")
    public String processPayment(@PathVariable Long orderId,
                              @RequestParam String paymentMethod,
                              @RequestParam(required = false) String cardNumber,
                              @RequestParam(required = false) String cardHolder,
                              @RequestParam(required = false) String expiryDate,
                              @RequestParam(required = false) String cvv,
                              RedirectAttributes redirectAttributes) {
        
        Order order = orderService.getOrderById(orderId);
        
        try {
            boolean paymentSuccess = false;
            
            if ("CREDIT_CARD".equals(paymentMethod)) {
                paymentSuccess = paymentService.processCardPayment(order, cardNumber, cardHolder, expiryDate, cvv);
            } else if ("PAYPAL".equals(paymentMethod)) {
                paymentSuccess = paymentService.processPayPalPayment(order);
            } else if ("COD".equals(paymentMethod)) {
                paymentSuccess = paymentService.processCashOnDelivery(order);
            }
            
            if (paymentSuccess) {
                orderService.updateOrderStatus(orderId, "PROCESSING");
                redirectAttributes.addFlashAttribute("successMessage", "Payment processed successfully!");
                return "redirect:/orders/" + orderId + "/confirmation";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment failed. Please try again.");
                return "redirect:/payment/process/" + orderId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment error: " + e.getMessage());
            return "redirect:/payment/process/" + orderId;
        }
    }
    
    @GetMapping("/success")
    public String paymentSuccess() {
        return "payment-success";
    }
    
    @GetMapping("/failed")
    public String paymentFailed() {
        return "payment-failed";
    }
}
