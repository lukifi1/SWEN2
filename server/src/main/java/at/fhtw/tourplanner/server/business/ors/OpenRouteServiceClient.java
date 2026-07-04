package at.fhtw.tourplanner.server.business.ors;

import at.fhtw.tourplanner.server.business.exceptions.RouteCalculationException;
import at.fhtw.tourplanner.server.config.TourPlannerProperties;
import at.fhtw.tourplanner.server.dto.LocationSuggestionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Thin client around the OpenRouteService REST API. Geocodes the textual
 * "from"/"to" locations into coordinates and then asks the Directions API for
 * the distance, duration and geometry of the route.
 */
@Component
@Slf4j
public class OpenRouteServiceClient {

    private final RestClient restClient;
    private final String apiKey;

    public OpenRouteServiceClient(RestClient orsRestClient, TourPlannerProperties properties) {
        this.restClient = orsRestClient;
        this.apiKey = properties.getOrs().getApiKey();
    }

    public RouteResult calculateRoute(String from, String to, String transportType) {
        if (!StringUtils.hasText(apiKey)) {
            throw new RouteCalculationException(
                    "OpenRouteService API key is not configured. Set OPENROUTE_API_KEY in your environment.");
        }

        double[] fromCoord = geocode(from);   // [lon, lat]
        double[] toCoord = geocode(to);
        String profile = TransportProfileResolver.resolveProfile(transportType);

        DirectionsResponse response;
        try {
            response = restClient.post()
                    .uri("/v2/directions/{profile}/geojson", profile)
                    .header("Authorization", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DirectionsRequest(List.of(
                            List.of(fromCoord[0], fromCoord[1]),
                            List.of(toCoord[0], toCoord[1]))))
                    .retrieve()
                    .body(DirectionsResponse.class);
        } catch (Exception ex) {
            throw new RouteCalculationException(
                    "Failed to calculate the route via OpenRouteService.", ex);
        }

        if (response == null || response.features() == null || response.features().isEmpty()) {
            throw new RouteCalculationException("OpenRouteService returned no route.");
        }

        DirectionsResponse.Feature feature = response.features().getFirst();
        double distanceKm = feature.properties().summary().distance() / 1000.0;
        double timeHours = feature.properties().summary().duration() / 3600.0;
        String geometryJson = toLatLngJson(feature.geometry().coordinates());

        log.info("ORS route {} -> {} ({}): {} km, {} h",
                from, to, profile, String.format("%.1f", distanceKm), String.format("%.2f", timeHours));
        return new RouteResult(distanceKm, timeHours, geometryJson);
    }

    public List<LocationSuggestionDto> suggestLocations(String query) {
        if (!StringUtils.hasText(query) || query.trim().length() < 3) {
            return List.of();
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new RouteCalculationException(
                    "OpenRouteService API key is not configured. Set OPENROUTE_API_KEY in your environment.");
        }

        GeocodeResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/geocode/autocomplete")
                            .queryParam("api_key", apiKey)
                            .queryParam("text", query.trim())
                            .queryParam("size", 5)
                            .build())
                    .retrieve()
                    .body(GeocodeResponse.class);
        } catch (Exception ex) {
            throw new RouteCalculationException("Failed to retrieve location suggestions.", ex);
        }

        if (response == null || response.features() == null || response.features().isEmpty()) {
            return List.of();
        }

        return response.features().stream()
                .map(this::toSuggestion)
                .filter(suggestion -> StringUtils.hasText(suggestion.label()))
                .toList();
    }

    public Optional<String> reverseGeocode(double lat, double lon) {
        if (!StringUtils.hasText(apiKey)) {
            throw new RouteCalculationException(
                    "OpenRouteService API key is not configured. Set OPENROUTE_API_KEY in your environment.");
        }

        GeocodeResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/geocode/reverse")
                            .queryParam("api_key", apiKey)
                            .queryParam("point.lon", lon)
                            .queryParam("point.lat", lat)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .body(GeocodeResponse.class);
        } catch (Exception ex) {
            throw new RouteCalculationException("Failed to reverse-geocode coordinates.", ex);
        }

        if (response == null || response.features() == null || response.features().isEmpty()) {
            return Optional.empty();
        }

        LocationSuggestionDto suggestion = toSuggestion(response.features().getFirst());
        return StringUtils.hasText(suggestion.label())
                ? Optional.of(shortLocationLabel(suggestion.label()))
                : Optional.empty();
    }

    private String shortLocationLabel(String label) {
        String[] parts = label.split(",");
        if (parts.length < 2) {
            return label.trim();
        }
        return parts[0].trim() + ", " + parts[1].trim();
    }

    /** Returns coordinates as [lon, lat] for the given location, as ORS expects. */
    private double[] geocode(String location) {
        GeocodeResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/geocode/search")
                            .queryParam("api_key", apiKey)
                            .queryParam("text", location)
                            .queryParam("size", 1)
                            .build())
                    .retrieve()
                    .body(GeocodeResponse.class);
        } catch (Exception ex) {
            throw new RouteCalculationException("Failed to geocode location: " + location, ex);
        }

        if (response == null || response.features() == null || response.features().isEmpty()) {
            throw new RouteCalculationException("Could not find a location named: " + location);
        }
        List<Double> coords = response.features().getFirst().geometry().coordinates();
        return new double[]{coords.get(0), coords.get(1)};
    }

    /** Converts ORS [lon, lat] pairs into a JSON array of [lat, lng] pairs for Leaflet. */
    private String toLatLngJson(List<List<Double>> coordinates) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < coordinates.size(); i++) {
            List<Double> point = coordinates.get(i);
            double lon = point.get(0);
            double lat = point.get(1);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('[').append(lat).append(',').append(lon).append(']');
        }
        return sb.append(']').toString();
    }

    // --- ORS request/response shapes (only the fields we need) ---

    record DirectionsRequest(List<List<Double>> coordinates) {
    }

    record DirectionsResponse(List<Feature> features) {
        record Feature(Geometry geometry, Properties properties) {
        }

        record Geometry(List<List<Double>> coordinates) {
        }

        record Properties(Summary summary) {
        }

        record Summary(double distance, double duration) {
        }
    }

    private LocationSuggestionDto toSuggestion(GeocodeResponse.Feature feature) {
        List<Double> coords = feature.geometry() == null ? List.of() : feature.geometry().coordinates();
        Double lon = coords.size() > 0 ? coords.get(0) : null;
        Double lat = coords.size() > 1 ? coords.get(1) : null;
        String label = feature.properties() == null ? null : feature.properties().label();
        if (!StringUtils.hasText(label) && feature.properties() != null) {
            label = feature.properties().name();
        }
        return new LocationSuggestionDto(label, lon, lat);
    }

    record GeocodeResponse(List<Feature> features) {
        record Feature(Geometry geometry, Properties properties) {
        }

        record Geometry(List<Double> coordinates) {
        }

        record Properties(String label, String name) {
        }
    }
}
