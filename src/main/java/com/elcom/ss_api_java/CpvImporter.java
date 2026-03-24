package com.elcom.ss_api_java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

@Service
public class CpvImporter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String importCpvData(String filePath) {
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // FIX: Look for the individual "CPV" items, not the giant wrapper!
            NodeList nList = doc.getElementsByTagName("CPV");
            int count = 0;

            // Force Java to ensure the table exists before starting
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS cpv_codes (id INT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(20) NOT NULL, description TEXT NOT NULL)");
            
            // Clear any old/broken data
            jdbcTemplate.execute("TRUNCATE TABLE cpv_codes");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String code = eElement.getAttribute("CODE");
                    
                    // Find the English text description
                    NodeList textNodes = eElement.getElementsByTagName("TEXT");
                    String description = "";
                    for (int j = 0; j < textNodes.getLength(); j++) {
                        Element txt = (Element) textNodes.item(j);
                        if (txt.getAttribute("LANG").equals("EN")) {
                            description = txt.getTextContent();
                            break;
                        }
                    }

                    jdbcTemplate.update("INSERT INTO cpv_codes (code, description) VALUES (?, ?)", code, description);
                    count++;
                }
            }
            return "Successfully imported " + count + " CPV codes.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}