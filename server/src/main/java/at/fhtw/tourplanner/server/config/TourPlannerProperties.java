package at.fhtw.tourplanner.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed access to all externalized application configuration.
 * Every value is supplied through application.properties which in turn reads
 * from environment variables (.env) - nothing is hard-coded in the source.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "tourplanner")
public class TourPlannerProperties {

    private final Jwt jwt = new Jwt();
    private final Ors ors = new Ors();
    private final Image image = new Image();
    private final Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {
        /** Secret key used to sign JWT tokens (min 32 characters for HS256). */
        private String secret;
        /** Token lifetime in milliseconds. */
        private long expirationMs = 86_400_000L;
    }

    @Getter
    @Setter
    public static class Ors {
        /** Base URL of the OpenRouteService API. */
        private String baseUrl = "https://api.openrouteservice.org";
        /** API key for OpenRouteService. */
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Image {
        /** Filesystem directory where tour images are stored. */
        private String basePath;
    }

    @Getter
    @Setter
    public static class Cors {
        /** Comma separated list of allowed frontend origins. */
        private String allowedOrigins = "http://localhost:4200";
    }
}
