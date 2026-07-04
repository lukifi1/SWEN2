package at.fhtw.tourplanner.server.dto;

public record LocationSuggestionDto(
        String label,
        Double longitude,
        Double latitude
) {
}
