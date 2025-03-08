package luanlocweb.groud.flower_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private BigDecimal price;
    
    private String imageUrl;
    
    private int stockQuantity;
    
    private int salesCount; // added field to track the number of sales
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private boolean featured;
    
    private boolean active = true;
}