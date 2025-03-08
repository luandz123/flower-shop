package luanlocweb.groud.flower_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import luanlocweb.groud.flower_shop.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByResetToken(String resetToken);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(String role);
    
    List<User> findByActive(boolean active);
}