package com.example.usermanagement;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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
        admin.setRoles("Admin", "User");

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user-password"));
        user.setEmail("user@domain.com");
        user.setEnabled(true);
        user.setRoles("User");

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
    void shouldGetAuthorizedForAdminAccessingAdminEndpoint() throws Exception {
        String expectedJson = """
        [
            {"username": "admin","email": "admin@domain.com"},
            {"username": "user","email": "user@domain.com"}
        ]
        """;

        mockMvc
            .perform(get("/home/admin/all-users").with(httpBasic("admin", "admin-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$..username").value(containsInAnyOrder("admin", "user")))
            .andExpect(jsonPath("$..email").value(
                containsInAnyOrder("admin@domain.com", "user@domain.com")))
            .andExpect(content().json(expectedJson));

    }
}
