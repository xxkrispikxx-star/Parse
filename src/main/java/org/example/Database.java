package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String URL = "jdbc:sqlite:prices.db";

    public Database() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String createTable = "CREATE TABLE IF NOT EXISTS price_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "price TEXT," +
                    "date TEXT," +
                    "is_favorite INTEGER DEFAULT 0," +
                    "UNIQUE(name, price)" +
                    ");";
            conn.createStatement().execute(createTable);
        } catch (SQLException e) {
            System.out.println("Ошибка БД: " + e.getMessage());
        }
    }

    public void saveProduct(Product p) {
        String sql = "INSERT OR IGNORE INTO price_history(name, price, date) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getPrice());
            pstmt.setString(3, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public void toggleLike(String name) {
        String sql = "UPDATE price_history SET is_favorite = 1 - is_favorite WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка лайка: " + e.getMessage());
        }
    }
    public List<Map<String, String>> getFavorites() {
        List<Map<String, String>> history = new ArrayList<>();
        String sql = "SELECT name, price, date FROM price_history WHERE is_favorite = 1 ORDER BY id DESC";


        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("price", rs.getString("price"));
                row.put("date", rs.getString("date"));
                history.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения избранного: " + e.getMessage());
        }
        return history;
    }


    public List<Map<String, String>> getAllHistory() {
        return getFavorites();
    }
    public String getLastPrice(String productName) {
        String sql = "SELECT price FROM price_history WHERE name = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("price");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения старой цены: " + e.getMessage());
        }
        return null;
    }}