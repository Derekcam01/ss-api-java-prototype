package com.elcom.ss_api_java;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CpvImporter cpvImporter;

    private void ensureTablesExist() {
        // Table 1: Search Profiles
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS search_profiles (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(255), " +
            "keyword TEXT, " +
            "excluded_keywords TEXT, " +
            "min_value BIGINT, " +
            "max_value BIGINT, " +
            "regions VARCHAR(255), " +
            "notice_types VARCHAR(255), " +
            "cpv_codes TEXT, " +
            "expiry_from VARCHAR(50), " +
            "expiry_to VARCHAR(50)" +
        ")");
        
        // Table 2: The New Sales CRM Tracking Board!
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tracked_notices (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "ocid VARCHAR(255) UNIQUE, " +
            "notes TEXT, " +
            "raw_json LONGTEXT" +
        ")");
    }

    // ==========================================
    // EXISTING PROFILES ENDPOINTS
    // ==========================================
    @GetMapping("/profiles/import-cpv")
    public String runImport() { return cpvImporter.importCpvData("cpv_2008.xml"); }

    @GetMapping("/profiles/cpv-search")
    public List<Map<String, Object>> searchCpv(@RequestParam String q) {
        String term = q.trim().toLowerCase();
        String query = "SELECT code, description FROM cpv_codes " +
                       "WHERE LOWER(description) LIKE ? OR code LIKE ? " +
                       "ORDER BY CASE WHEN LOWER(description) LIKE ? THEN 1 WHEN LOWER(description) LIKE ? THEN 2 ELSE 3 END, description ASC LIMIT 25";
        return jdbcTemplate.queryForList(query, "%" + term + "%", term + "%", term + "%", "% " + term + "%");
    }

    @PostMapping("/profiles/save")
    public Map<String, String> saveProfile(@RequestBody Map<String, Object> req) {
        ensureTablesExist(); 
        String sql = "INSERT INTO search_profiles (name, keyword, excluded_keywords, min_value, max_value, regions, notice_types, cpv_codes, expiry_from, expiry_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, req.get("name"), req.get("keyword"), req.get("excluded_keywords"), req.get("min_value"), req.get("max_value"), req.get("regions"), req.get("notice_types"), req.get("cpv_codes"), req.get("expiry_from"), req.get("expiry_to"));
        return Map.of("status", "success");
    }

    @PutMapping("/profiles/edit/{id}")
    public Map<String, String> editProfile(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        ensureTablesExist(); 
        String sql = "UPDATE search_profiles SET name=?, keyword=?, excluded_keywords=?, min_value=?, max_value=?, regions=?, notice_types=?, cpv_codes=?, expiry_from=?, expiry_to=? WHERE id=?";
        jdbcTemplate.update(sql, req.get("name"), req.get("keyword"), req.get("excluded_keywords"), req.get("min_value"), req.get("max_value"), req.get("regions"), req.get("notice_types"), req.get("cpv_codes"), req.get("expiry_from"), req.get("expiry_to"), id);
        return Map.of("status", "success");
    }

    @GetMapping("/profiles/list")
    public List<Map<String, Object>> listProfiles() {
        ensureTablesExist(); 
        return jdbcTemplate.queryForList("SELECT * FROM search_profiles ORDER BY id DESC");
    }

    @DeleteMapping("/profiles/delete/{id}")
    public Map<String, String> deleteProfile(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM search_profiles WHERE id = ?", id);
        return Map.of("status", "deleted");
    }

    // ==========================================
    // NEW TRACKED PIPELINE ENDPOINTS
    // ==========================================
    @PostMapping("/tracked/save")
    public Map<String, String> saveTracked(@RequestBody Map<String, Object> req) {
        ensureTablesExist();
        String sql = "INSERT IGNORE INTO tracked_notices (ocid, notes, raw_json) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, req.get("ocid"), req.get("notes"), req.get("raw_json"));
        return Map.of("status", "success");
    }

    @GetMapping("/tracked/list")
    public List<Map<String, Object>> listTracked() {
        ensureTablesExist();
        return jdbcTemplate.queryForList("SELECT * FROM tracked_notices ORDER BY id DESC");
    }

    @PutMapping("/tracked/notes/{ocid}")
    public Map<String, String> updateNotes(@PathVariable String ocid, @RequestBody Map<String, Object> req) {
        jdbcTemplate.update("UPDATE tracked_notices SET notes = ? WHERE ocid = ?", req.get("notes"), ocid);
        return Map.of("status", "success");
    }

    @DeleteMapping("/tracked/delete/{ocid}")
    public Map<String, String> deleteTracked(@PathVariable String ocid) {
        jdbcTemplate.update("DELETE FROM tracked_notices WHERE ocid = ?", ocid);
        return Map.of("status", "deleted");
    }
}