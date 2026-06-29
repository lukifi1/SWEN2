package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.PersistenceFailedException;
import at.fhtw.tourplanner.server.business.exceptions.TourLogNotFoundException;
import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.business.mapper.TourLogMapper;
import at.fhtw.tourplanner.server.dal.TourLogRepository;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourLogCreateDto;
import at.fhtw.tourplanner.server.dto.TourLogResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for tour logs. Every log operation verifies that the parent
 * tour belongs to the authenticated user and triggers recomputation of the
 * tour's derived attributes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final ComputedAttributesService computedAttributesService;

    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getLogsForTour(Long tourId, String username) {
        requireOwnedTour(tourId, username);
        return tourLogRepository.findByTour_IdOrderByDateTimeDesc(tourId)
                .stream()
                .map(TourLogMapper::toResponse)
                .toList();
    }

    @Transactional
    public TourLogResponseDto createLog(Long tourId, TourLogCreateDto dto, String username) {
        Tour tour = requireOwnedTour(tourId, username);
        TourLog log = TourLog.builder()
                .dateTime(dto.dateTime())
                .comment(dto.comment())
                .difficulty(dto.difficulty())
                .totalDistance(dto.totalDistance())
                .totalTime(dto.totalTime())
                .rating(dto.rating())
                .tour(tour)
                .build();

        TourLog saved = save(log);
        computedAttributesService.recompute(tour);
        TourLogService.log.info("Created log {} for tour {}", saved.getId(), tourId);
        return TourLogMapper.toResponse(saved);
    }

    @Transactional
    public TourLogResponseDto updateLog(Long tourId, Long logId, TourLogCreateDto dto, String username) {
        Tour tour = requireOwnedTour(tourId, username);
        TourLog log = loadOwnedLog(tourId, logId, username);

        log.setDateTime(dto.dateTime());
        log.setComment(dto.comment());
        log.setDifficulty(dto.difficulty());
        log.setTotalDistance(dto.totalDistance());
        log.setTotalTime(dto.totalTime());
        log.setRating(dto.rating());

        TourLog saved = save(log);
        computedAttributesService.recompute(tour);
        return TourLogMapper.toResponse(saved);
    }

    @Transactional
    public void deleteLog(Long tourId, Long logId, String username) {
        Tour tour = requireOwnedTour(tourId, username);
        TourLog log = loadOwnedLog(tourId, logId, username);
        try {
            tourLogRepository.delete(log);
        } catch (DataAccessException ex) {
            throw new PersistenceFailedException("Failed to delete tour log " + logId, ex);
        }
        computedAttributesService.recompute(tour);
        TourLogService.log.info("Deleted log {} from tour {}", logId, tourId);
    }

    private Tour requireOwnedTour(Long tourId, String username) {
        return tourRepository.findByIdAndUser_Username(tourId, username)
                .orElseThrow(() -> new TourNotFoundException(tourId));
    }

    private TourLog loadOwnedLog(Long tourId, Long logId, String username) {
        TourLog log = tourLogRepository.findByIdAndTour_User_Username(logId, username)
                .orElseThrow(() -> new TourLogNotFoundException(logId));
        if (!log.getTour().getId().equals(tourId)) {
            throw new TourLogNotFoundException(logId);
        }
        return log;
    }

    private TourLog save(TourLog log) {
        try {
            return tourLogRepository.save(log);
        } catch (DataAccessException ex) {
            throw new PersistenceFailedException("Failed to save tour log", ex);
        }
    }
}
