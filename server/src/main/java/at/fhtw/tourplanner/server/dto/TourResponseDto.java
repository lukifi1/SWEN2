package at.fhtw.tourplanner.server.dto;

public record TourResponseDto(
        Long id,
        String name,
        String description,
        String fromLocation,
        String toLocation,
        String transportType,
        Double distance,
        Double estimatedTime,
        String routeInformation,
        String imagePath,
        Integer popularity,
        Double childFriendliness
) {
}
