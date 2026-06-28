package at.fhtw.tourplanner.server.dto;

import java.time.LocalDateTime;

public record TourLogResponseDto(
        Long id,
        Long tourId,
        LocalDateTime dateTime,
        String comment,
        Integer difficulty,
        Double totalDistance,
        Double totalTime,
        Integer rating
) {
}
