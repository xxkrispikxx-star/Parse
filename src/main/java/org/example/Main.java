package org.example;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        SpringApplication.run(Main.class, args);
        Database db = new Database();
        String query = "";
        String searchUrl = "https://hotline.ua/ua/sr/?q=" + query;
        System.setOut(new PrintStream(System.out, true, "UTF-8"));


        try {
            System.out.println("Trying hotline: " + searchUrl);

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(15000)
                    .get();
            System.out.println("DEBUG: Title -> " + doc.title());
            System.out.println("DEBUG: HTML length -> " + doc.html().length());
            System.out.println("Success! Connected to: " + doc.title());
            Parser myParser = new Parser();
            List<Product> results = myParser.parseHotline(doc);
            System.out.println("------------------------------");
            System.out.println("ИТОГО НАЙДЕНО: " + results.size());
            System.out.println("------------------------------");

            for (Product p : results) {
                System.out.println(p);
            }
            for (Product p : results) {

                String oldPrice = db.getLastPrice(p.getName());

                if (oldPrice == null || !oldPrice.equals(p.getPrice())) {
                    db.saveProduct(p);
                    System.out.println("НОВАЯ ЦЕНА или ТОВАР: " + p.getName());
                }
            }
            Product bestOffer = results.stream()
                    .min(Comparator.comparingInt(p -> myParser.parseMinPrice(p.getPrice())))
                    .orElse(null);

            if (bestOffer != null) {
                int currentMin = myParser.parseMinPrice(bestOffer.getPrice());

                System.out.println("\n--- МОНИТОРИНГ ЦЕН ---");
                System.out.println("Модель: " + bestOffer.getName());
                System.out.println("Текущая цена: " + currentMin + " грн");


                String lastPriceStr = db.getLastPrice(bestOffer.getName());
                int lastPrice = (lastPriceStr == null || lastPriceStr.isEmpty()) ? 0 : myParser.parseMinPrice(lastPriceStr);

                if (lastPrice != 0 && currentMin < lastPrice) {
                    System.out.println("🔥 ЦЕНА УПАЛА! Старая цена была: " + lastPrice + " грн");
                } else if (lastPrice != 0 && currentMin > lastPrice) {
                    System.out.println("📈 Цена выросла. Раньше было: " + lastPrice + " грн");
                } else {
                    System.out.println("✅ Цена стабильна.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error again: " + e.getMessage());
        }
        }

}

