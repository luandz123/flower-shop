package luanlocweb.groud.flower_shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Redirect the root path to the products page
     * @return redirect to products page
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }
}
