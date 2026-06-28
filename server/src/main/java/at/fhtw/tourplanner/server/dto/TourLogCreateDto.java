package at.fhtw.tourplanner.server.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TourLogCreateDto(
        @NotNull LocalDateTime dateTime,
        @Size(max = 3000) String comment,
        @NotNull @Min(1) @Max(5) Integer difficulty,
        @NotNull @PositiveOrZero Double totalDistance,
        @NotNull @PositiveOrZero Double totalTime,
        @NotNull @Min(1) @Max(5) Integer rating
) {
}
