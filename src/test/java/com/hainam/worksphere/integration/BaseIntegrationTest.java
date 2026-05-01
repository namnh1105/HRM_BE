package com.hainam.worksphere.integration;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.auth.config.JwtProperties;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.authorization.repository.RoleRepository;
import com.hainam.worksphere.authorization.repository.UserRoleRepository;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@Tag("integration")
@EnabledIfSystemProperty(
        named = "integration.tests.enabled",
        matches = "true",
        disabledReason = "Integration tests require Docker. Run with -Dintegration.tests.enabled=true and ensure Docker is running."
)
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        if (org.testcontainers.DockerClientFactory.instance().isDockerAvailable()) {
            POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("worksphere_test")
                    .withUsername("test")
                    .withPassword("test");
            POSTGRESQL_CONTAINER.start();
        } else {
            POSTGRESQL_CONTAINER = null;
        }
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        if (POSTGRESQL_CONTAINER != null && POSTGRESQL_CONTAINER.isRunning()) {
            registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        } else {
            // Fallback to H2 if Docker is not available
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        }
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtProperties jwtProperties;

    @BeforeEach
    void baseIntegrationSetUp() {
        SecurityContextHolder.clearContext();
        ensureRolesSeeded();
        truncateTablesForIsolation();
    }

    private void ensureRolesSeeded() {
        if (!roleRepository.existsByCode("USER")) {
            roleRepository.save(Role.builder()
                    .code("USER")
                    .displayName("User")
                    .isSystem(true)
                    .isActive(true)
                    .build());
        }
        if (!roleRepository.existsByCode("ADMIN")) {
            roleRepository.save(Role.builder()
                    .code("ADMIN")
                    .displayName("Administrator")
                    .isSystem(true)
                    .isActive(true)
                    .build());
        }
    }

    protected String createJwtTokenByRole(String role) {
        String roleCode = mapRoleAliasToCode(role);

        User user = User.builder()
                .email("it-" + roleCode.toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID() + "@example.com")
                .password(passwordEncoder.encode("Password123"))
                .isEnabled(true)
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);

        Role dbRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy role trong DB: " + roleCode));

        userRoleRepository.save(UserRole.builder()
                .userId(savedUser.getId())
                .role(dbRole)
                .isActive(true)
                .build());

        List<Permission> permissions = permissionRepository.findByRoleCode(roleCode);
        UserPrincipal principal = UserPrincipal.create(savedUser, List.of(dbRole), permissions);
        return jwtUtil.generateAccessToken(principal);
    }

    protected String bearerTokenByRole(String role) {
        return "Bearer " + createJwtTokenByRole(role);
    }

        protected String bearerExpiredTokenByRole(String role) {
        String roleCode = mapRoleAliasToCode(role);

        User user = User.builder()
            .email("expired-" + roleCode.toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID() + "@example.com")
            .password(passwordEncoder.encode("Password123"))
            .isEnabled(true)
            .isDeleted(false)
            .build();

        User savedUser = userRepository.save(user);

        Role dbRole = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy role trong DB: " + roleCode));

        userRoleRepository.save(UserRole.builder()
            .userId(savedUser.getId())
            .role(dbRole)
            .isActive(true)
            .build());

        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - 60_000);
        Date issuedAt = new Date(now.getTime() - 120_000);

        String expiredToken = Jwts.builder()
            .setSubject(savedUser.getEmail())
            .setIssuedAt(issuedAt)
            .setExpiration(expiredAt)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return "Bearer " + expiredToken;
        }

    private String mapRoleAliasToCode(String roleAlias) {
        if (roleAlias == null || roleAlias.isBlank()) {
            return "USER";
        }

        return switch (roleAlias.trim().toLowerCase(Locale.ROOT)) {
            case "staff" -> "USER";
            case "manager" -> "ADMIN";
            default -> roleAlias.trim().toUpperCase(Locale.ROOT);
        };
    }

    private void truncateTablesForIsolation() {
        try {
            boolean isPostgres = jdbcTemplate.getDataSource().getConnection().getMetaData()
                    .getDatabaseProductName().toLowerCase().contains("postgres");

            List<String> tables;
            if (isPostgres) {
                tables = jdbcTemplate.queryForList(
                        """
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public'
                          AND tablename NOT IN ('roles', 'permissions', 'role_permissions', 'flyway_schema_history')
                        """,
                        String.class
                );
            } else {
                // H2 fallback
                tables = jdbcTemplate.queryForList(
                        """
                        SELECT TABLE_NAME
                        FROM INFORMATION_SCHEMA.TABLES
                        WHERE TABLE_SCHEMA = 'PUBLIC'
                          AND TABLE_TYPE = 'TABLE'
                          AND TABLE_NAME NOT IN ('roles', 'permissions', 'role_permissions', 'flyway_schema_history')
                        """,
                        String.class
                );
            }

            if (tables.isEmpty()) {
                return;
            }

            String tableList = tables.stream()
                    .map(table -> "\"" + table + "\"")
                    .collect(Collectors.joining(", "));

            if (isPostgres) {
                jdbcTemplate.execute("TRUNCATE TABLE " + tableList + " RESTART IDENTITY CASCADE");
            } else {
                jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
                for (String table : tables) {
                    jdbcTemplate.execute("TRUNCATE TABLE \"" + table + "\"");
                }
                jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
        } catch (Exception e) {
            // Log and ignore or handle appropriately
            System.err.println("Failed to truncate tables: " + e.getMessage());
        }
    }
}
