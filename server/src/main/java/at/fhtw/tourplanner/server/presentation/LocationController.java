package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.ors.OpenRouteServiceClient;
import at.fhtw.tourplanner.server.dto.LocationSuggestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final OpenRouteServiceClient openRouteServiceClient;

    @GetMapping("/suggest")
    public List<LocationSuggestionDto> suggest(@RequestParam(name = "q", defaultValue = "") String query) {
        return openRouteServiceClient.suggestLocations(query);
    }
}
