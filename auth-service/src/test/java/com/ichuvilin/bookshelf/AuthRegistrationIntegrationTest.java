package com.ichuvilin.bookshelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichuvilin.bookshelf.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class AuthRegistrationIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("auth_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUserAndStorePasswordHash() throws Exception {
        String password = "StrongPassword123!";

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("reader", "reader@example.com", password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.username").value("reader"))
                .andExpect(jsonPath("$.user.email").value("reader@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        Integer usersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                "reader@example.com"
        );
        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE email = ?",
                String.class,
                "reader@example.com"
        );

        assertThat(usersCount).isEqualTo(1);
        assertThat(storedPassword)
                .isNotEqualTo(password)
                .startsWith("$2");
    }

    @Test
    void register_shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        register("reader", "reader@example.com", "StrongPassword123!");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("another-reader", "reader@example.com", "AnotherPass123!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Email")));
    }

    @Test
    void register_shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        register("reader", "reader@example.com", "StrongPassword123!");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("reader", "another@example.com", "AnotherPass123!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("reader", "not-an-email", "StrongPassword123!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    void register_shouldRejectBlankRequiredFields() throws Exception {
        String request = """
                {
                  "username": " ",
                  "email": "",
                  "password": " "
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("username")))
                .andExpect(jsonPath("$.message", containsString("email")))
                .andExpect(jsonPath("$.message", containsString("password")));
    }

    @Test
    void register_shouldRejectPasswordShorterThanEightCharacters() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("reader", "reader@example.com", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("password")));
    }

    private void register(String username, String email, String password) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest(username, email, password)))
                .andExpect(status().isCreated());
    }

    private String registerRequest(String username, String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new RegisterPayload(username, email, password));
    }

    private record RegisterPayload(String username, String email, String password) {
    }
}
