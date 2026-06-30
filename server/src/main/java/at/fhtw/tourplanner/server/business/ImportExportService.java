package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.importexport.ImportExportStrategy;
import at.fhtw.tourplanner.server.business.importexport.TourExportFile;
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

/**
 * Imports and exports the authenticated user's tours (including their logs).
 * The serialization format is provided by an {@link ImportExportStrategy}
 * (Strategy pattern), currently JSON.
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

    @Transactional(readOnly = true)
    public byte[] export(String username) {
        List<Tour> tours = tourRepository.findByUser_UsernameOrderByIdDesc(username);
        List<TourExportFile.TourExport> exportTours = tours.stream()
                .map(this::toExport)
                .toList();
        log.info("Exporting {} tours for user '{}'", exportTours.size(), username);
        return strategy.export(new TourExportFile(EXPORT_VERSION, exportTours));
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
                .fromLocation(exportTour.fromLocation())
                .toLocation(exportTour.toLocation())
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
}
