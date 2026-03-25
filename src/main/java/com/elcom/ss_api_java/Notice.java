package com.elcom.ss_api_java;

import jakarta.persistence.*;

@Entity
@Table(name = "fts_notices")
public class Notice {

    @Id
    @Column(name = "ocid")
    private String ocid; // The unique government ID

    @Column(name = "raw_json", columnDefinition = "LONGTEXT")
    private String rawJson; // The massive chunk of data

    // --- Getters and Setters ---
    public String getOcid() {
        return ocid;
    }

    public void setOcid(String ocid) {
        this.ocid = ocid;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }
}