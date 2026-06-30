package at.fhtw.tourplanner.server.business.ors;

import at.fhtw.tourplanner.server.business.exceptions.RouteCalculationException;
import at.fhtw.tourplanner.server.config.TourPlannerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Protects the response mapping of {@link OpenRouteServiceClient} against a mocked HTTP
 * layer. Verifies the full call sequence (geocode-from, geocode-to, directions), the
 * unit conversions (meters -> km, seconds -> hours), and the geometry rewrite from ORS
 * [lon,lat] pairs into Leaflet-ready [lat,lng] JSON. Also pins the fast-fail when no API
 * key is configured (a {@link RouteCalculationException} without any HTTP call).
 */
class OpenRouteServiceClientTest {

    private static TourPlannerProperties propertiesWithKey(String apiKey) {
        TourPlannerProperties properties = new TourPlannerProperties();
        properties.getOrs().setBaseUrl("https://api.openrouteservice.org");
        properties.getOrs().setApiKey(apiKey);
        return properties;
    }

    @Test
    void calculateRouteMapsDistanceTimeAndGeometry() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenRouteServiceClient ors = new OpenRouteServiceClient(restClient, propertiesWithKey("real-key"));

        // 1) geocode(from) -> [lon, lat] = [16.3, 48.2]
        server.expect(requestTo(containsString("/geocode/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"features\":[{\"geometry\":{\"coordinates\":[16.3,48.2]}}]}",
                        MediaType.APPLICATION_JSON));
        // 2) geocode(to) -> [lon, lat] = [15.4, 47.0]
        server.expect(requestTo(containsString("/geocode/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"features\":[{\"geometry\":{\"coordinates\":[15.4,47.0]}}]}",
                        MediaType.APPLICATION_JSON));
        // 3) directions -> 200000 m, 7200 s, two [lon,lat] points
        server.expect(requestTo(containsString("/v2/directions/driving-car/geojson")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"features\":[{\"geometry\":{\"coordinates\":[[16.3,48.2],[15.4,47.0]]},"
                                + "\"properties\":{\"summary\":{\"distance\":200000.0,\"duration\":7200.0}}}]}",
                        MediaType.APPLICATION_JSON));

        RouteResult result = ors.calculateRoute("Vienna", "Graz", "Car");

        server.verify();
        assertThat(result.distanceKm()).isEqualTo(200.0);   // 200000 / 1000
        assertThat(result.timeHours()).isEqualTo(2.0);       // 7200 / 3600
        // geometry rewritten to [lat, lng] order
        assertThat(result.geometryJson()).isEqualTo("[[48.2,16.3],[47.0,15.4]]");
    }

    @Test
    void blankApiKeyFailsFastWithoutHttpCall() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenRouteServiceClient ors = new OpenRouteServiceClient(restClient, propertiesWithKey("  "));

        assertThatThrownBy(() -> ors.calculateRoute("Vienna", "Graz", "Car"))
                .isInstanceOf(RouteCalculationException.class);

        // no request should have been issued
        server.verify();
    }
}
