package luanlocweb.groud.flower_shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import luanlocweb.groud.flower_shop.model.User;
import luanlocweb.groud.flower_shop.service.UserService;


@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        // Nếu người dùng đã đăng nhập, chuyển hướng về trang chủ
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            return "register";
        }
        
        if (user.getEmail() != null && userService.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email already in use");
            return "register";
        }
        
        try {
            userService.registerUser(user);
            
            // Auto-login sau khi đăng ký
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Welcome to Flower Shop.");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        model.addAttribute("user", user);
        return "profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                             RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        
        user.setFullName(updatedUser.getFullName());
        user.setAddress(updatedUser.getAddress());
        user.setPhone(updatedUser.getPhone());
        
        userService.updateUser(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                      RedirectAttributes redirectAttributes) {
        boolean isEmailSent = userService.processForgotPassword(email);
        
        if (isEmailSent) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "We have sent a password reset link to your email. Please check your inbox.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Email address not found in our records.");
        }
        
        return "redirect:/forgot-password";
    }
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        boolean isValidToken = userService.validatePasswordResetToken(token);
        
        if (!isValidToken) {
            model.addAttribute("errorMessage", 
                "Invalid or expired password reset token. Please request a new one.");
            return "redirect:/forgot-password";
        }
        
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                     @RequestParam("password") String password,
                                     @RequestParam("confirmPassword") String confirmPassword,
                                     RedirectAttributes redirectAttributes) {
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match");
            return "redirect:/reset-password?token=" + token;
        }
        
        boolean isSuccess = userService.resetPassword(token, password);
        
        if (isSuccess) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Your password has been reset successfully. Please login with your new password.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error occurred while resetting your password. Please try again.");
            return "redirect:/forgot-password";
        }
    }
    
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }
    
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("currentPassword") String currentPassword,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/change-password";
        }
        
        boolean isSuccess = userService.changePassword(username, currentPassword, newPassword);
        
        if (isSuccess) {
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed successfully.");
            return "redirect:/profile";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect or an error occurred. Please try again.");
            return "redirect:/change-password";
        }
    }
}