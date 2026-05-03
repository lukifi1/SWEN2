package at.fhtw.tourplanner.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TourCreateDto(
        @NotBlank String name,
        String description,
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        @NotBlank String transportType,
        @NotNull @PositiveOrZero Double distance,
        @NotNull @PositiveOrZero Double estimatedTime,
        String routeInformation,
        String imagePath
) {
}
