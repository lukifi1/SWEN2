package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.StatsService;
import at.fhtw.tourplanner.server.dto.StatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public StatsDto getStats(Authentication auth) {
        return statsService.computeStats(auth.getName());
    }
}
