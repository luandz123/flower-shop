package luanlocweb.groud.flower_shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import luanlocweb.groud.flower_shop.model.BlogPost;
import luanlocweb.groud.flower_shop.repository.BlogRepository;
import java.util.List;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;
    
    public List<BlogPost> getAllPosts() {
        return blogRepository.findAll();
    }
    
    public List<BlogPost> getAllActivePosts() {
        return blogRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }
    
    public List<BlogPost> getRecentPosts(int count) {
        // Với câu lệnh trong repository, số lượng là 5 cố định; nếu muốn thay đổi theo count, bạn cần tùy chỉnh truy vấn.
        return blogRepository.findTop5ByPublishedTrueOrderByCreatedAtDesc();
    }
    
    public BlogPost getPostById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));
    }
    
    public BlogPost savePost(BlogPost post) {
        return blogRepository.save(post);
    }
    
    public void deletePost(Long id) {
        BlogPost post = getPostById(id);
        post.setPublished(false);
        blogRepository.save(post);
    }
}