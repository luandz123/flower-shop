package luanlocweb.groud.flower_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import luanlocweb.groud.flower_shop.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    
    List<Order> findByStatus(String status);
    
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN ?1 AND ?2")
    BigDecimal getRevenueBetweenDates(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2")
    Long getOrderCountBetweenDates(LocalDateTime start, LocalDateTime end);
}