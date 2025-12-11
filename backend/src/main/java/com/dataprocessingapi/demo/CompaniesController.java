package com.dataprocessingapi.demo;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class CompaniesController {
    private final JdbcTemplate jdbc;

    public CompaniesController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping(path = "/api/companies")
    @ResponseStatus(HttpStatus.CREATED)
    public Company create(@RequestBody CreateCompany req) {
        if (req == null || !StringUtils.hasText(req.name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        Long id = jdbc.queryForObject(
                "INSERT INTO companies (name, license, discountRecieved) VALUES (?, ?, ?) RETURNING id",
                Long.class,
                req.name.trim(),
                req.license,
                req.discountReceived != null ? req.discountReceived : Boolean.FALSE
        );
        return new Company(id,
                req.name.trim(),
                req.license,
                req.discountReceived != null ? req.discountReceived : Boolean.FALSE);
    }

    @GetMapping(path = "/api/companies")
    public List<Company> list() {
        RowMapper<Company> mapper = (rs, i) -> new Company(
                rs.getLong("id"),
                rs.getString("name"),
                (Long) rs.getObject("license"),
                rs.getBoolean("discountRecieved")
        );
        return jdbc.query("SELECT id, name, license, discountRecieved FROM companies ORDER BY id DESC", mapper);
    }

    public static class CreateCompany {
        public String name;
        public Long license;
        public Boolean discountReceived;
    }

    public record Company(Long id, String name, Long license, Boolean discountReceived) { }
}
