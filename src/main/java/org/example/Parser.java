package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public List<Product> parseHotline(Document doc) {
        List<Product> products = new ArrayList<>();


        Elements elements = doc.select(".product-item, [class*='product-item'], .list-item");

        for (Element el : elements) {

            String name = el.select("a.link--black, .list-item__title, h3, .p-30-link").text().trim();

            String price = el.select(".price-view__value, [class*='price'], .text-orange").text().trim();

            // 3. Ссылка
            String link = el.select("a.link--black, a[href*='/ua/']").first() != null
                    ? el.select("a.link--black, a[href*='/ua/']").first().attr("abs:href")
                    : "";



            if (!name.isEmpty()) {
                products.add(new Product(name, price, link));
            }
        }
        return products;
    }

    public int parseMinPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return Integer.MAX_VALUE;

        String clean = priceStr.split("–")[0].replaceAll("[^0-9]", "");

        return clean.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(clean);

    }

    }
