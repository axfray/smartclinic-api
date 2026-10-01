package com.smartclinic.api.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Convierte DATABASE_URL (formato Render: postgresql://user:password@host:port/db)
 * en spring.datasource.url JDBC, sin credenciales (que van por DB_USERNAME/DB_PASSWORD).
 * No hace nada si DB_URL ya está definida.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String DB_URL = "DB_URL";
    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private static final int DEFAULT_POSTGRES_PORT = 5432;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getProperty(DB_URL) != null) {
            return;
        }

        Optional<String> jdbcUrl = toJdbcUrl(environment.getProperty(DATABASE_URL));
        if (jdbcUrl.isEmpty()) {
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(SPRING_DATASOURCE_URL, jdbcUrl.get());
        environment.getPropertySources().addFirst(new MapPropertySource("databaseUrl", properties));
    }

    static Optional<String> toJdbcUrl(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return Optional.empty();
        }

        URI uri;
        try {
            uri = new URI(databaseUrl);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equals("postgres") || scheme.equals("postgresql"))) {
            return Optional.empty();
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return Optional.empty();
        }
        if (host.indexOf(':') >= 0) {
            host = "[" + host + "]";
        }

        String path = uri.getPath();
        if (path == null || path.isBlank() || path.equals("/")) {
            return Optional.empty();
        }

        int port = uri.getPort() == -1 ? DEFAULT_POSTGRES_PORT : uri.getPort();
        String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();

        return Optional.of("jdbc:postgresql://" + host + ":" + port + path + query);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
