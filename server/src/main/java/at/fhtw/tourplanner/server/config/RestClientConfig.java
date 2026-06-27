package at.fhtw.tourplanner.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /** RestClient pre-configured with the OpenRouteService base URL. */
    @Bean
    public RestClient orsRestClient(TourPlannerProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getOrs().getBaseUrl())
                .build();
    }
}
