package at.fhtw.tourplanner.server.business.exceptions;

/**
 * Raised when the route (distance / time / geometry) could not be retrieved
 * from OpenRouteService - e.g. a location could not be geocoded, the API key
 * is missing/invalid, or the service is unreachable.
 */
public class RouteCalculationException extends BusinessException {

    public RouteCalculationException(String message) {
        super(message);
    }

    public RouteCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
