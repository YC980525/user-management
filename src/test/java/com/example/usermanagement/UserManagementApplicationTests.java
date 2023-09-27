package com.example.usermanagement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class UserManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserManagementRepository userManagementRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeAll
    public void createTestUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin-password"));
        admin.setEmail("admin@domain.com");
        admin.setEnabled(true);
        admin.setRoles("ADMIN", "USER");

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user-password"));
        user.setEmail("user@domain.com");
        user.setEnabled(true);
        user.setRoles("USER");

        userManagementRepository.save(admin);
        userManagementRepository.save(user);
    }

    @Test
    void shouldGetUnauthorizedForAnonymousUser() throws Exception {
        mockMvc
            .perform(get("/home"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void shouldGetNotFoundForNonExistEndpoint() throws Exception {
        mockMvc
            .perform(get("/home/non-exist-endpoint").with(httpBasic("user", "user-password")))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetForbiddenForUserAccessingAdminEndpoint() throws Exception {
        mockMvc
            .perform(get("/home/admin/all-users").with(httpBasic("user", "user-password")))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetOkForAdminAccessingAllUsers() throws Exception {
        String expectedJson = """
            [
                {"username": "admin", "email": "admin@domain.com"},
                {"username": "user", "email": "user@domain.com"}
            ]
            """;

        mockMvc
            .perform(get("/home/admin/all-users").with(httpBasic("admin", "admin-password")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson, true));
    }

    @Test
    void shouldGetBadRequestForCreatingExistingUser() throws Exception {

        User user = new User();
        user.setUsername("user");
        user.setPassword("user-password");
        user.setEmail("user@domain.com");

        mockMvc
            .perform(
                post("/home/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(user)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetNotFoundForAccessingOtherUserEndpoint() throws Exception {

        mockMvc
            .perform(
                get("/home/dummy").with(httpBasic("admin", "admin-password")))
            .andExpectAll(
                status().isNotFound());

    }

    @Test
    void shouldRedirectForCreatingNewUser() throws Exception {

        String expectedJson = """
                {"username": "dummy", "email": "dummy@domain.com"}
            """;
        String inputJson = """
                {"username": "dummy", "email": "dummy@domain.com", "password": "dummy-password"}
            """;

        MvcResult mvcResult = mockMvc
            .perform(
                post("/home/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson))
            .andExpectAll(
                status().isSeeOther(),
                redirectedUrl("http://localhost/home/dummy"))
            .andReturn();

        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();

        mockMvc
            .perform(get(redirectUrl).with(httpBasic("dummy", "dummy-password")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));
    }
}
