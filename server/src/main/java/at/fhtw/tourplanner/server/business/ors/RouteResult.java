package at.fhtw.tourplanner.server.business.ors;

/**
 * Result of an OpenRouteService route calculation.
 *
 * @param distanceKm   total route distance in kilometers
 * @param timeHours    estimated travel time in hours
 * @param geometryJson route geometry as a JSON array of [lat, lng] pairs (ready for Leaflet)
 */
public record RouteResult(double distanceKm, double timeHours, String geometryJson) {
}
