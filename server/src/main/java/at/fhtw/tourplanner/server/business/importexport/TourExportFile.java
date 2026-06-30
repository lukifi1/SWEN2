package at.fhtw.tourplanner.server.business.importexport;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Self-contained, format-agnostic representation of a user's tours used for
 * import and export. The concrete serialization (JSON, ...) is handled by an
 * {@link ImportExportStrategy}.
 */
public record TourExportFile(
        String version,
        List<TourExport> tours
) {
    public record TourExport(
            String name,
            String description,
            String fromLocation,
            String toLocation,
            String transportType,
            Double distance,
            Double estimatedTime,
            String routeGeometry,
            String imagePath,
            List<TourLogExport> logs
    ) {
    }

    public record TourLogExport(
            LocalDateTime dateTime,
            String comment,
            Integer difficulty,
            Double totalDistance,
            Double totalTime,
            Integer rating
    ) {
    }
}
