package luanlocweb.groud.flower_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    private int quantity;
    
    private BigDecimal price;  // Price at time of purchase
    
    private BigDecimal subtotal;
    
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        this.subtotal = price.multiply(new BigDecimal(quantity));
    }
}
