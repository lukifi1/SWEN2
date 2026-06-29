package at.fhtw.tourplanner.server.business.ors;

/**
 * Maps a user-facing transport type onto the routing profile understood by
 * the OpenRouteService Directions API.
 */
public final class TransportProfileResolver {

    private TransportProfileResolver() {
    }

    public static String resolveProfile(String transportType) {
        if (transportType == null) {
            return "driving-car";
        }
        String type = transportType.toLowerCase();
        if (type.contains("hik")) {
            return "foot-hiking";
        }
        if (type.contains("walk") || type.contains("run")) {
            return "foot-walking";
        }
        if (type.contains("bike") || type.contains("cycl") || type.contains("bicycle")) {
            return "cycling-regular";
        }
        // car, vacation, driving, anything else
        return "driving-car";
    }
}
