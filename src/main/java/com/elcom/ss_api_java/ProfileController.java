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

    @PostMapping("/save")
    public Map<String, String> saveProfile(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        jdbcTemplate.update("INSERT INTO search_profiles (keyword) VALUES (?)", keyword);
        return Map.of("status", "success");
    }

    @GetMapping("/list")
    public List<Map<String, Object>> listProfiles() {
        return jdbcTemplate.queryForList("SELECT * FROM search_profiles ORDER BY id DESC");
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, String> deleteProfile(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM search_profiles WHERE id = ?", id);
        return Map.of("status", "deleted");
    }
}