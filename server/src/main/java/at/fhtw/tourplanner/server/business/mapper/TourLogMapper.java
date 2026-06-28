package at.fhtw.tourplanner.server.business.mapper;

import at.fhtw.tourplanner.server.dto.TourLogResponseDto;
import at.fhtw.tourplanner.server.model.TourLog;

public final class TourLogMapper {

    private TourLogMapper() {
    }

    public static TourLogResponseDto toResponse(TourLog log) {
        return new TourLogResponseDto(
                log.getId(),
                log.getTour() != null ? log.getTour().getId() : null,
                log.getDateTime(),
                log.getComment(),
                log.getDifficulty(),
                log.getTotalDistance(),
                log.getTotalTime(),
                log.getRating());
    }
}
