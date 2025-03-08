package luanlocweb.groud.flower_shop.controller;

import luanlocweb.groud.flower_shop.model.BlogPost;
import luanlocweb.groud.flower_shop.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;
    
    @GetMapping
    public String viewBlogPosts(Model model) {
        model.addAttribute("posts", blogService.getAllActivePosts());
        return "blog";
    }
    
    @GetMapping("/{id}")
    public String viewBlogPost(@PathVariable Long id, Model model) {
        BlogPost post = blogService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", blogService.getRecentPosts(5));
        return "post";
    }
    
    @GetMapping("/admin")
    public String manageBlogPosts(Model model) {
        model.addAttribute("posts", blogService.getAllPosts());
        return "admin/blog-posts";
    }
    
    @GetMapping("/admin/add")
    public String showAddPostForm(Model model) {
        model.addAttribute("post", new BlogPost());
        return "admin/blog-form";
    }
    
    @PostMapping("/admin/add")
    public String addBlogPost(@ModelAttribute BlogPost post,
                          @RequestParam("postImage") MultipartFile file,
                          RedirectAttributes redirectAttributes) {
        if (!file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/blog");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.write(uploadPath.resolve(fileName), file.getBytes());
                post.setImageUrl("/uploads/blog/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
            }
        }
        blogService.savePost(post);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post added successfully!");
        return "redirect:/blog/admin";
    }
    
    @GetMapping("/admin/edit/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model) {
        BlogPost post = blogService.getPostById(id);
        model.addAttribute("post", post);
        return "admin/blog-form";
    }
    
    @PostMapping("/admin/edit/{id}")
    public String updateBlogPost(@PathVariable Long id,
                           @ModelAttribute BlogPost post,
                           @RequestParam("postImage") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        BlogPost existingPost = blogService.getPostById(id);
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setSummary(post.getSummary());
        if (!file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/blog");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.write(uploadPath.resolve(fileName), file.getBytes());
                existingPost.setImageUrl("/uploads/blog/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
            }
        }
        blogService.savePost(existingPost);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post updated successfully!");
        return "redirect:/blog/admin";
    }
    
    @GetMapping("/admin/delete/{id}")
    public String deleteBlogPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogService.deletePost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post deleted successfully!");
        return "redirect:/blog/admin";
    }
}