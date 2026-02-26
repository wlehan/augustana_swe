package com.augustana.golf.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/health/db")
    public String db() throws Exception {
        try (var conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "DB OK" : "DB NOT OK";
        }
    }
}