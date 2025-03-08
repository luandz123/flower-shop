package luanlocweb.groud.flower_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(length = 500)
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String imageUrl;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;  // Đảm bảo lớp User dùng ở đây nằm trong package model
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    private boolean published = true;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}