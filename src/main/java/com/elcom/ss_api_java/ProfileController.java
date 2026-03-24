package com.elcom.ss_api_java;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CpvImporter cpvImporter;

    // THE FIX: This forces the database to build the correct buckets right before saving
    private void ensureTableExists() {
        // 1. Create the table if it doesn't exist at all
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS search_profiles (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(255), " +
            "keyword TEXT, " +
            "excluded_keywords TEXT, " +
            "min_value BIGINT, " +
            "max_value BIGINT, " +
            "regions VARCHAR(255), " +
            "notice_types VARCHAR(255), " +
            "cpv_codes TEXT" +
        ")");
        
        // 2. Safely add the 'name' column if the table is from our older version
        try {
            jdbcTemplate.execute("ALTER TABLE search_profiles ADD COLUMN name VARCHAR(255) AFTER id");
        } catch (Exception e) {
            // If it throws an error, it just means the column is already there. Safe to ignore!
        }
    }

    @GetMapping("/import-cpv")
    public String runImport() {
        return cpvImporter.importCpvData("cpv_2008.xml");
    }

    @GetMapping("/cpv-search")
    public List<Map<String, Object>> searchCpv(@RequestParam String q) {
        String query = "SELECT code, description FROM cpv_codes WHERE LOWER(description) LIKE LOWER(?) OR code LIKE ? LIMIT 10";
        return jdbcTemplate.queryForList(query, "%" + q + "%", q + "%");
    }

    @PostMapping("/save")
    public Map<String, String> saveProfile(@RequestBody Map<String, Object> req) {
        ensureTableExists(); // Ensure the database is ready!
        String sql = "INSERT INTO search_profiles (name, keyword, excluded_keywords, min_value, max_value, regions, notice_types, cpv_codes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, req.get("name"), req.get("keyword"), req.get("excluded_keywords"), req.get("min_value"), req.get("max_value"), req.get("regions"), req.get("notice_types"), req.get("cpv_codes"));
        return Map.of("status", "success");
    }

    @PutMapping("/edit/{id}")
    public Map<String, String> editProfile(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        ensureTableExists(); // Ensure the database is ready!
        String sql = "UPDATE search_profiles SET name=?, keyword=?, excluded_keywords=?, min_value=?, max_value=?, regions=?, cpv_codes=? WHERE id=?";
        jdbcTemplate.update(sql, req.get("name"), req.get("keyword"), req.get("excluded_keywords"), req.get("min_value"), req.get("max_value"), req.get("regions"), req.get("cpv_codes"), id);
        return Map.of("status", "success");
    }

    @GetMapping("/list")
    public List<Map<String, Object>> listProfiles() {
        ensureTableExists(); // Ensure the database is ready!
        return jdbcTemplate.queryForList("SELECT * FROM search_profiles ORDER BY id DESC");
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, String> deleteProfile(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM search_profiles WHERE id = ?", id);
        return Map.of("status", "deleted");
    }
}