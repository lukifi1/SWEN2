package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.RouteCalculationException;
import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.business.importexport.ImportExportStrategy;
import at.fhtw.tourplanner.server.business.importexport.TourExportFile;
import at.fhtw.tourplanner.server.business.ors.OpenRouteServiceClient;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import at.fhtw.tourplanner.server.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Imports and exports the authenticated user's tours (including their logs).
 * The serialization format is provided by an {@link ImportExportStrategy}
 * (Strategy pattern), currently GPX.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private static final String EXPORT_VERSION = "1.0";

    private final TourRepository tourRepository;
    private final CurrentUserService currentUserService;
    private final ComputedAttributesService computedAttributesService;
    private final ImportExportStrategy strategy;
    private final OpenRouteServiceClient openRouteServiceClient;

    @Transactional(readOnly = true)
    public byte[] export(String username) {
        List<Tour> tours = tourRepository.findByUser_UsernameOrderByIdDesc(username);
        List<TourExportFile.TourExport> exportTours = tours.stream()
                .map(this::toExport)
                .toList();
        log.info("Exporting {} tours for user '{}'", exportTours.size(), username);
        return strategy.export(new TourExportFile(EXPORT_VERSION, exportTours));
    }

    @Transactional(readOnly = true)
    public byte[] exportTour(Long tourId, String username) {
        Tour tour = tourRepository.findByIdAndUser_Username(tourId, username)
                .orElseThrow(() -> new TourNotFoundException(tourId));
        log.info("Exporting tour {} ('{}') for user '{}'", tour.getId(), tour.getName(), username);
        return strategy.export(new TourExportFile(EXPORT_VERSION, List.of(toExport(tour))));
    }

    public String exportFileExtension() {
        return strategy.fileExtension();
    }

    public String exportContentType() {
        return strategy.contentType();
    }

    @Transactional
    public int importData(byte[] data, String username) {
        User user = currentUserService.require(username);
        TourExportFile file = strategy.parse(data);
        if (file.tours() == null || file.tours().isEmpty()) {
            return 0;
        }

        int imported = 0;
        for (TourExportFile.TourExport exportTour : file.tours()) {
            Tour tour = toEntity(exportTour, user);
            Tour saved = tourRepository.save(tour);
            computedAttributesService.recompute(saved);
            imported++;
        }
        log.info("Imported {} tours for user '{}'", imported, username);
        return imported;
    }

    private TourExportFile.TourExport toExport(Tour tour) {
        List<TourExportFile.TourLogExport> logs = tour.getLogs().stream()
                .map(l -> new TourExportFile.TourLogExport(
                        l.getDateTime(), l.getComment(), l.getDifficulty(),
                        l.getTotalDistance(), l.getTotalTime(), l.getRating()))
                .toList();
        return new TourExportFile.TourExport(
                tour.getName(), tour.getDescription(), tour.getFromLocation(),
                tour.getToLocation(), tour.getTransportType(), tour.getDistance(),
                tour.getEstimatedTime(), tour.getRouteGeometry(), tour.getImagePath(), logs);
    }

    private Tour toEntity(TourExportFile.TourExport exportTour, User user) {
        Tour tour = Tour.builder()
                .name(exportTour.name())
                .description(exportTour.description())
                .fromLocation(resolveImportedLocation(exportTour.fromLocation()))
                .toLocation(resolveImportedLocation(exportTour.toLocation()))
                .transportType(exportTour.transportType())
                .distance(exportTour.distance())
                .estimatedTime(exportTour.estimatedTime())
                .routeGeometry(exportTour.routeGeometry())
                .imagePath(exportTour.imagePath())
                .popularity(0)
                .childFriendliness(0.0)
                .user(user)
                .build();

        List<TourLog> logs = new ArrayList<>();
        if (exportTour.logs() != null) {
            for (TourExportFile.TourLogExport l : exportTour.logs()) {
                logs.add(TourLog.builder()
                        .dateTime(l.dateTime())
                        .comment(l.comment())
                        .difficulty(l.difficulty())
                        .totalDistance(l.totalDistance())
                        .totalTime(l.totalTime())
                        .rating(l.rating())
                        .tour(tour)
                        .build());
            }
        }
        tour.setLogs(logs);
        return tour;
    }

    private String resolveImportedLocation(String location) {
        Optional<CoordinatePair> coordinatePair = parseCoordinatePair(location);
        if (coordinatePair.isEmpty()) {
            return location;
        }

        CoordinatePair coordinates = coordinatePair.get();
        try {
            return openRouteServiceClient.reverseGeocode(coordinates.lat(), coordinates.lon())
                    .orElse(location);
        } catch (RouteCalculationException ex) {
            log.warn("Could not resolve imported GPX coordinates '{}': {}", location, ex.getMessage());
            return location;
        }
    }

    private Optional<CoordinatePair> parseCoordinatePair(String location) {
        if (location == null || location.isBlank()) {
            return Optional.empty();
        }
        String[] parts = location.split(",");
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(new CoordinatePair(
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim())));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private record CoordinatePair(double lat, double lon) {}
}
