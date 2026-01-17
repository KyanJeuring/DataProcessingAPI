package com.fleetmaster.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetmaster.entities.Company;
import com.fleetmaster.repositories.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyRepository companyRepository;

    private Company company1;
    private Company company2;

    @BeforeEach
    void setUp() {
        company1 = new Company();
        company1.setId(1L);
        company1.setName("Tech Transport Ltd");
        company1.setLicense("LIC-001");
        company1.setDiscountReceived(true);

        company2 = new Company();
        company2.setId(2L);
        company2.setName("Fast Shipping Inc");
        company2.setLicense("LIC-002");
        company2.setDiscountReceived(false);
    }

    @Test
    void testListCompanies_Success() throws Exception {
        // Given
        List<Company> companies = Arrays.asList(company1, company2);
        when(companyRepository.findAll()).thenReturn(companies);

        // When & Then
        mockMvc.perform(get("/api/companies")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Tech Transport Ltd"))
                .andExpect(jsonPath("$[1].name").value("Fast Shipping Inc"));
    }

    @Test
    void testListCompanies_EmptyList() throws Exception {
        // Given
        when(companyRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/companies")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testListCompanies_XMLResponse() throws Exception {
        // Given
        List<Company> companies = Arrays.asList(company1);
        when(companyRepository.findAll()).thenReturn(companies);

        // When & Then
        mockMvc.perform(get("/api/companies")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    void testCreateCompany_Success() throws Exception {
        // Given
        Company newCompany = new Company();
        newCompany.setName("New Logistics Co");
        newCompany.setLicense("LIC-003");
        newCompany.setDiscountReceived(true);

        Company savedCompany = new Company();
        savedCompany.setId(3L);
        savedCompany.setName("New Logistics Co");
        savedCompany.setLicense("LIC-003");
        savedCompany.setDiscountReceived(true);

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Logistics Co"))
                .andExpect(jsonPath("$.license").value("LIC-003"))
                .andExpect(jsonPath("$.discountReceived").value(true));
    }

    @Test
    void testCreateCompany_WithTrimmedName() throws Exception {
        // Given
        Company newCompany = new Company();
        newCompany.setName("  Trimmed Company  ");
        newCompany.setLicense("LIC-004");

        Company savedCompany = new Company();
        savedCompany.setId(4L);
        savedCompany.setName("Trimmed Company");
        savedCompany.setLicense("LIC-004");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Trimmed Company"));
    }

    @Test
    void testCreateCompany_NullName_BadRequest() throws Exception {
        // Given
        Company invalidCompany = new Company();
        invalidCompany.setLicense("LIC-005");
        // name is null

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCompany)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCompany_EmptyName_BadRequest() throws Exception {
        // Given
        Company invalidCompany = new Company();
        invalidCompany.setName("");
        invalidCompany.setLicense("LIC-006");

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCompany)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCompany_WhitespaceName_BadRequest() throws Exception {
        // Given
        Company invalidCompany = new Company();
        invalidCompany.setName("   ");
        invalidCompany.setLicense("LIC-007");

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCompany)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCompany_JSONResponse() throws Exception {
        // Given
        Company newCompany = new Company();
        newCompany.setName("JSON Company");
        newCompany.setLicense("LIC-008");

        Company savedCompany = new Company();
        savedCompany.setId(5L);
        savedCompany.setName("JSON Company");
        savedCompany.setLicense("LIC-008");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateCompany_XMLResponse() throws Exception {
        // Given
        Company newCompany = new Company();
        newCompany.setName("XML Company");
        newCompany.setLicense("LIC-009");

        Company savedCompany = new Company();
        savedCompany.setId(6L);
        savedCompany.setName("XML Company");
        savedCompany.setLicense("LIC-009");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .content(objectMapper.writeValueAsString(newCompany)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }
}
