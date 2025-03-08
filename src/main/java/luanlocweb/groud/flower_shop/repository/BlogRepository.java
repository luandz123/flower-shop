package luanlocweb.groud.flower_shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import luanlocweb.groud.flower_shop.model.BlogPost;

import java.util.List;

public interface BlogRepository extends JpaRepository<BlogPost, Long> {
    
    List<BlogPost> findByPublishedTrueOrderByCreatedAtDesc();
    
    List<BlogPost> findTop5ByPublishedTrueOrderByCreatedAtDesc();
    
    List<BlogPost> findByAuthorId(Long authorId);
    
    List<BlogPost> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword);
}