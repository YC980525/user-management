package com.example.usermanagement;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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

    @Autowired
    TestRestTemplate restTemplate;

    @SpyBean
    PasswordResetService passwordResetService;

    @BeforeEach
    public void resetUserData() {
        userManagementRepository.deleteAll();

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
    void shouldGetOkForAdminAccessingAllUsersEndpoint() throws Exception {
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
                content().json(expectedJson));
    }

    @Test
    void shouldGetOkForAccesingOwnResource() throws Exception {
        String expectedJson = """
                {"username": "user", "email": "user@domain.com"}
            """;

        mockMvc
            .perform(get("/home/user/profile").with(httpBasic("user", "user-password")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

    }

    @Test
    void shouldGetNotFoundForAccessingOtherUserEndpoint() throws Exception {

        mockMvc
            .perform(
                get("/home/dummy/profile").with(httpBasic("admin", "admin-password")))
            .andExpectAll(
                status().isForbidden());

    }

    @Test
    void shouldGetBadRequestForCreatingExistingUser() throws Exception {

        String inputJson = """
                {"username": "user", "email": "user@domain.com", "password": "user-password"}
            """;

        mockMvc
            .perform(
                post("/home/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson)
                    .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOkForFetchingSignUpEndpoint() throws Exception {
        mockMvc
            .perform(get("/home/sign-up"))
            .andExpectAll(
                status().isOk(),
                cookie().doesNotExist("XSRF-TOKEN"));

    }

    @Test
    void shouldGetCreatedAndRedirectForCreatingNewUser() throws Exception {

        String expectedJson = """
                {"username": "newUser", "email": "newUser@domain.com"}
            """;
        String inputJson = """
                {
                    "username": "newUser",
                    "email": "newUser@domain.com",
                    "password": "newUser-password"
                }
            """;

        MvcResult mvcResult = mockMvc
            .perform(
                post("/home/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson)
                    .with(csrf()))
            .andExpectAll(
                status().isCreated(),
                redirectedUrl("http://localhost/home/newUser/profile"))
            .andReturn();


        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();

        mockMvc
            .perform(get(redirectUrl).with(httpBasic("newUser", "newUser-password")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

        resetUserData();
    }

    @Test
    void shouldGetOkForFetchingLogInEndpoint() throws Exception {
        mockMvc
            .perform(get("/home/login"))
            .andExpectAll(
                status().isOk(),
                cookie().doesNotExist("XSRF-TOKEN"));

    }

    @Test
    void shouldGetOkandRedirectedAfterLogin() throws Exception {

        String expectedJson = """
                {"username": "user", "email": "user@domain.com"}
            """;

        MvcResult mvcResult = mockMvc.perform(
            post("/home/login")
                .with(httpBasic("user", "user-password"))
                .with(csrf()))
            .andExpectAll(
                status().isFound(),
                redirectedUrl("http://localhost/home/user/profile"))
            .andReturn();

        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();
        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc
            .perform(get(redirectUrl).session(session))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

        mockMvc
            .perform(post("/home/logout").session(session).with(csrf()))
            .andExpectAll(
                status().isNoContent(),
                unauthenticated());

    }

    @Test
    void shouldGetUnauthorizedForFirstSessionAfterSecondLogin() throws Exception {

        MvcResult mvcResult = mockMvc
            .perform(
                post("/home/login")
                    .with(httpBasic("user", "user-password"))
                    .with(csrf()))
            .andReturn();

        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();
        MockHttpSession firstSession = (MockHttpSession) mvcResult.getRequest().getSession();
        mvcResult = mockMvc
            .perform(
                post("/home/login")
                    .with(httpBasic("user", "user-password"))
                    .with(csrf()))
            .andReturn();

        MockHttpSession secondSession = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc
            .perform(get(redirectUrl).session(firstSession))
            .andExpectAll(
                status().isOk(),
                unauthenticated());

        mockMvc
            .perform(get(redirectUrl).session(secondSession))
            .andExpectAll(
                status().isOk(),
                authenticated());

    }

    @Test
    void shouldGetOkForUpdatingProfileAndMustLogInAgain() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
            post("/home/login")
                .with(httpBasic("user", "user-password"))
                .with(csrf()))
            .andReturn();

        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        String expectedJson = """
                {"username": "user", "email": "updatedUser@domain.com"}
            """;

        String inputJson = """
                {"password": "updatedPassword", "email": "updatedUser@domain.com"}
            """;

        mockMvc
            .perform(
                patch("/home/user/update")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson)
                    .with(csrf()))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

        mockMvc
            .perform(get("/home/user/profile").session(session))
            .andExpectAll(
                status().isUnauthorized(),
                unauthenticated());

        mockMvc
            .perform(get("/home/user/profile").with(httpBasic("user", "updatedPassword")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

    }

    @Test
    void shouldDeleteUserProfileAndReturnNoContent() throws Exception {

        MvcResult mvcResult = mockMvc.perform(
            post("/home/login")
                .with(httpBasic("user", "user-password"))
                .with(csrf()))
            .andReturn();

        MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc
            .perform(delete("/home/user/delete").session(session).with(csrf()))
            .andExpectAll(
                status().isNoContent());

        mockMvc
            .perform(get("/home/user/profile").session(session))
            .andExpectAll(
                status().isUnauthorized());

        mockMvc
            .perform(get("/home/user/profile").with(httpBasic("user", "user-password")))
            .andExpectAll(
                status().isUnauthorized());

    }

    @Test
    void shouldResetPasswordAfterRequestingForgetPassword() throws Exception {

        User user = userManagementRepository.findByEmail("user@domain.com");
        PasswordResetToken testToken = new PasswordResetToken(UUID.randomUUID().toString(), user);
        when(passwordResetService.generateToken(user)).thenReturn(testToken);

        String inputJson = """
            {"email": "user@domain.com"}
            """;

        mockMvc
            .perform(
                post("/home/forget-password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson))
            .andExpectAll(
                status().isOk());

        inputJson = """
            {"password": "updatedPassword"}
            """;

        mockMvc
            .perform(
                post("/home/reset-password")
                    .param("token", testToken.getTokenValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(inputJson)
                    .with(csrf()))
            .andExpectAll(
                status().isOk());

        String expectedJson = """
            {"username": "user", "email": "user@domain.com"}
            """;

        mockMvc
            .perform(get("/home/user/profile").with(httpBasic("user", "updatedPassword")))
            .andExpectAll(
                status().isOk(),
                content().json(expectedJson));

    }

}
