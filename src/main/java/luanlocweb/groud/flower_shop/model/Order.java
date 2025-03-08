package luanlocweb.groud.flower_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Đổi tên trường từ totalAmount -> total để truy vấn SUM(o.total) hoạt động
    private BigDecimal total;
    
    private String shippingAddress;
    
    private String paymentMethod;
    
    private String status = "PENDING";  // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    
    private String notes;
    
    // Tính tổng đơn hàng và lưu vào trường total
    public void calculateTotal() {
        this.total = orderItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}