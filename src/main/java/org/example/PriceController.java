package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class PriceController {

    private final Parser myParser = new Parser();
    private final Database db = new Database();

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("history", db.getFavorites());
        return "index";
    }

    @GetMapping("/like")
    public String toggleLike(@RequestParam String name, Model model) {
        db.toggleLike(name);
        model.addAttribute("history", db.getFavorites());
        return "index :: #mainContent";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        String searchUrl = "https://hotline.ua/ua/sr/?q=" + query;

        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0")
                    .get();
            List<Product> results = myParser.parseHotline(doc);

            Product bestOffer = results.stream()
                    .min(java.util.Comparator.comparingInt(p -> myParser.parseMinPrice(p.getPrice())))
                    .orElse(null);

            if (bestOffer != null) {
                db.saveProduct(bestOffer);
                model.addAttribute("product", bestOffer);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }

        model.addAttribute("history", db.getFavorites());

        return "index :: #mainContent";
    }
}