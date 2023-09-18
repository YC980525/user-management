package com.example.usermanagement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import lombok.extern.slf4j.Slf4j;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Slf4j
class UserManagementApplicationTests {
    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemberManagementApplicationTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldGetUnauthorizedForMockMVC() throws Exception {
        mockMvc.perform(get("/home/lalala"))
           .andExpect(status().isUnauthorized())
           .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void shouldGetUnauthorizedForHttpServer()  {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/home/all", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().containsKey("WWW-Authenticate"));
    }

    @Test
    void shouldAuthenticated()  {
    	ResponseEntity<String> response = restTemplate
    		.withBasicAuth("admin", "password")
    		.getForEntity("/home/all", String.class);
    	log.info(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().containsKey("WWW-Authenticate"));
    }
}
