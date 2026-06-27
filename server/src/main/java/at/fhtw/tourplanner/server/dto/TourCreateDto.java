package at.fhtw.tourplanner.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating/updating a tour. Distance, estimated time and route
 * geometry are intentionally absent: they are computed server-side from
 * OpenRouteService. {@code imagePath} references a previously uploaded image.
 */
public record TourCreateDto(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 3000) String description,
        @NotBlank @Size(max = 200) String fromLocation,
        @NotBlank @Size(max = 200) String toLocation,
        @NotBlank @Size(max = 60) String transportType,
        String imagePath
) {
}
