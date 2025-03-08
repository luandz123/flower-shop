package luanlocweb.groud.flower_shop.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import luanlocweb.groud.flower_shop.model.User;
import luanlocweb.groud.flower_shop.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) throws Exception {
        if (existsByUsername(user.getUsername()))
            throw new Exception("Username already exists");
        if (user.getEmail() != null && existsByEmail(user.getEmail()))
            throw new Exception("Email already exists");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    // Thêm phương thức này để AdminController có thể sử dụng
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token).orElse(null);
        if (user == null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
        return true;
    }
    
    public boolean processForgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);
        // TODO: Gửi email cho user chứa link reset mật khẩu với token này.
        return true;
    }
    
    public boolean validatePasswordResetToken(String token) {
        User user = userRepository.findByResetToken(token).orElse(null);
        return user != null;
    }
    
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if(user == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return getUserByUsername(username);
    }
}