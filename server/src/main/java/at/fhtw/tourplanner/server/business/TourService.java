package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourCreateDto;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {

    private final TourRepository tourRepository;

    public List<TourResponseDto> getAllTours() {
        log.debug("Fetching all tours");

        return tourRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public TourResponseDto getTourById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new TourNotFoundException(id));

        return mapToResponseDto(tour);
    }

    public TourResponseDto createTour(TourCreateDto dto) {
        Tour tour = Tour.builder()
                .name(dto.name())
                .description(dto.description())
                .fromLocation(dto.fromLocation())
                .toLocation(dto.toLocation())
                .transportType(dto.transportType())
                .distance(dto.distance())
                .estimatedTime(dto.estimatedTime())
                .routeInformation(dto.routeInformation())
                .imagePath(dto.imagePath())
                .popularity(0)
                .childFriendliness(0.0)
                .build();

        Tour savedTour = tourRepository.save(tour);

        log.info("Created tour with id {}", savedTour.getId());

        return mapToResponseDto(savedTour);
    }

    public TourResponseDto updateTour(Long id, TourCreateDto dto) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new TourNotFoundException(id));

        tour.setName(dto.name());
        tour.setDescription(dto.description());
        tour.setFromLocation(dto.fromLocation());
        tour.setToLocation(dto.toLocation());
        tour.setTransportType(dto.transportType());
        tour.setDistance(dto.distance());
        tour.setEstimatedTime(dto.estimatedTime());
        tour.setRouteInformation(dto.routeInformation());
        tour.setImagePath(dto.imagePath());

        Tour savedTour = tourRepository.save(tour);

        log.info("Updated tour with id {}", savedTour.getId());

        return mapToResponseDto(savedTour);
    }

    public void deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            throw new TourNotFoundException(id);
        }

        tourRepository.deleteById(id);

        log.info("Deleted tour with id {}", id);
    }

    public List<TourResponseDto> searchTours(String query) {
        return tourRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private TourResponseDto mapToResponseDto(Tour tour) {
        return new TourResponseDto(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getFromLocation(),
                tour.getToLocation(),
                tour.getTransportType(),
                tour.getDistance(),
                tour.getEstimatedTime(),
                tour.getRouteInformation(),
                tour.getImagePath(),
                tour.getPopularity(),
                tour.getChildFriendliness()
        );
    }
}
