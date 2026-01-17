package com.fleetmaster.controllers;

import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.entities.Info;
import com.fleetmaster.services.InfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InfoService infoService;

    private CompanyAccount companyAccount;
    private Authentication authentication;
    private List<Info> infoList;

    @BeforeEach
    void setUp() {
        companyAccount = new CompanyAccount();
        companyAccount.setId(1L);
        companyAccount.setEmail("test@company.com");
        companyAccount.setCompanyId(100L);
        companyAccount.setVerified(true);
        companyAccount.setAccountStatus("ACTIVE");

        Info info1 = new Info();
        info1.setId(1L);
        info1.setTitle("System Update");
        info1.setContent("New features available");

        Info info2 = new Info();
        info2.setId(2L);
        info2.setTitle("Maintenance Notice");
        info2.setContent("Scheduled maintenance on Sunday");

        infoList = Arrays.asList(info1, info2);
        
        authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(companyAccount);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testGetAllInfo_Success() throws Exception {
        // Given
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("System Update"))
                .andExpect(jsonPath("$[1].title").value("Maintenance Notice"));
    }

    @Test
    void testGetAllInfo_NotAuthenticated() throws Exception {
        // Given
        Authentication unauthenticated = org.mockito.Mockito.mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(unauthenticated)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void testGetAllInfo_NoAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/database/info/get"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void testGetAllInfo_NotVerified() throws Exception {
        // Given
        companyAccount.setVerified(false);
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Company account not verified"));
    }

    @Test
    void testGetAllInfo_BlockedAccount() throws Exception {
        // Given
        companyAccount.setAccountStatus("BLOCKED");
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Company account blocked"));
    }

    @Test
    void testGetAllInfo_EmptyList() throws Exception {
        // Given
        when(infoService.getAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetAllInfo_VerifiedAndActive_Success() throws Exception {
        // Given
        companyAccount.setVerified(true);
        companyAccount.setAccountStatus("ACTIVE");
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetAllInfo_PendingStatus_Success() throws Exception {
        // Given
        companyAccount.setVerified(true);
        companyAccount.setAccountStatus("PENDING");
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then - PENDING status should allow access (not BLOCKED)
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllInfo_SuspendedStatus_Success() throws Exception {
        // Given
        companyAccount.setVerified(true);
        companyAccount.setAccountStatus("SUSPENDED");
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then - SUSPENDED status should allow access (not BLOCKED)
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllInfo_MultipleInfoItems() throws Exception {
        // Given
        Info info3 = new Info();
        info3.setId(3L);
        info3.setTitle("New Feature");
        info3.setContent("Dashboard improvements");

        List<Info> manyInfos = Arrays.asList(
            infoList.get(0),
            infoList.get(1),
            info3
        );
        when(infoService.getAll()).thenReturn(manyInfos);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[2].title").value("New Feature"));
    }

    @Test
    void testGetAllInfo_ContentTypeJSON() throws Exception {
        // Given
        when(infoService.getAll()).thenReturn(infoList);

        // When & Then
        mockMvc.perform(get("/database/info/get")
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
