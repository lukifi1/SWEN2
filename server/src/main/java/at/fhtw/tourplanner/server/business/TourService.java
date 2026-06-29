package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.PersistenceFailedException;
import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.business.mapper.TourMapper;
import at.fhtw.tourplanner.server.business.ors.OpenRouteServiceClient;
import at.fhtw.tourplanner.server.business.ors.RouteResult;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourCreateDto;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Business logic for tours. Every operation is scoped to the authenticated
 * user, and create/update calls OpenRouteService to derive distance, time and
 * route geometry from the textual "from"/"to" locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {

    private final TourRepository tourRepository;
    private final CurrentUserService currentUserService;
    private final OpenRouteServiceClient openRouteServiceClient;

    @Transactional(readOnly = true)
    public List<TourResponseDto> getAllTours(String username) {
        log.debug("Fetching all tours for user '{}'", username);
        return tourRepository.findByUser_UsernameOrderByIdDesc(username)
                .stream()
                .map(TourMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourResponseDto getTourById(Long id, String username) {
        return TourMapper.toResponse(loadOwnedTour(id, username));
    }

    @Transactional
    public TourResponseDto createTour(TourCreateDto dto, String username) {
        User user = currentUserService.require(username);
        RouteResult route = openRouteServiceClient.calculateRoute(
                dto.fromLocation(), dto.toLocation(), dto.transportType());

        Tour tour = Tour.builder()
                .name(dto.name())
                .description(dto.description())
                .fromLocation(dto.fromLocation())
                .toLocation(dto.toLocation())
                .transportType(dto.transportType())
                .distance(route.distanceKm())
                .estimatedTime(route.timeHours())
                .routeGeometry(route.geometryJson())
                .imagePath(dto.imagePath())
                .popularity(0)
                .childFriendliness(0.0)
                .user(user)
                .build();

        Tour saved = save(tour);
        log.info("Created tour {} ('{}') for user '{}'", saved.getId(), saved.getName(), username);
        return TourMapper.toResponse(saved);
    }

    @Transactional
    public TourResponseDto updateTour(Long id, TourCreateDto dto, String username) {
        Tour tour = loadOwnedTour(id, username);

        boolean routeRelevantChange =
                !Objects.equals(tour.getFromLocation(), dto.fromLocation())
                        || !Objects.equals(tour.getToLocation(), dto.toLocation())
                        || !Objects.equals(tour.getTransportType(), dto.transportType());

        tour.setName(dto.name());
        tour.setDescription(dto.description());
        tour.setFromLocation(dto.fromLocation());
        tour.setToLocation(dto.toLocation());
        tour.setTransportType(dto.transportType());
        tour.setImagePath(dto.imagePath());

        if (routeRelevantChange) {
            RouteResult route = openRouteServiceClient.calculateRoute(
                    dto.fromLocation(), dto.toLocation(), dto.transportType());
            tour.setDistance(route.distanceKm());
            tour.setEstimatedTime(route.timeHours());
            tour.setRouteGeometry(route.geometryJson());
        }

        Tour saved = save(tour);
        log.info("Updated tour {} for user '{}'", saved.getId(), username);
        return TourMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTour(Long id, String username) {
        Tour tour = loadOwnedTour(id, username);
        try {
            tourRepository.delete(tour);
        } catch (DataAccessException ex) {
            throw new PersistenceFailedException("Failed to delete tour " + id, ex);
        }
        log.info("Deleted tour {} for user '{}'", id, username);
    }

    /** Loads a tour and enforces that it belongs to the given user. */
    private Tour loadOwnedTour(Long id, String username) {
        return tourRepository.findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new TourNotFoundException(id));
    }

    private Tour save(Tour tour) {
        try {
            return tourRepository.save(tour);
        } catch (DataAccessException ex) {
            throw new PersistenceFailedException("Failed to save tour", ex);
        }
    }
}
