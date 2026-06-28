package at.fhtw.tourplanner.server.business.mapper;

import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;

/**
 * Maps {@link Tour} entities to the response DTO exposed by the API.
 */
public final class TourMapper {

    private TourMapper() {
    }

    public static TourResponseDto toResponse(Tour tour) {
        return new TourResponseDto(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getFromLocation(),
                tour.getToLocation(),
                tour.getTransportType(),
                tour.getDistance(),
                tour.getEstimatedTime(),
                tour.getRouteGeometry(),
                tour.getImagePath(),
                tour.getPopularity(),
                tour.getChildFriendliness());
    }
}
