package at.fhtw.tourplanner.server.business.ors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protects the mapping of user-facing transport types onto OpenRouteService routing
 * profiles in {@link TransportProfileResolver}. A wrong profile would make ORS return
 * a route for the wrong mode of travel (e.g. a car route for a hiking tour), so the
 * keyword matching (case-insensitive, substring based) and the {@code driving-car}
 * default for unknown/null values are pinned here.
 */
class TransportProfileResolverTest {

    @Test
    void hikingResolvesToFootHiking() {
        assertThat(TransportProfileResolver.resolveProfile("Hiking")).isEqualTo("foot-hiking");
    }

    @Test
    void runningResolvesToFootWalking() {
        assertThat(TransportProfileResolver.resolveProfile("running")).isEqualTo("foot-walking");
    }

    @Test
    void bikeResolvesToCyclingRegular() {
        assertThat(TransportProfileResolver.resolveProfile("Bike")).isEqualTo("cycling-regular");
    }

    @Test
    void cyclingResolvesToCyclingRegular() {
        assertThat(TransportProfileResolver.resolveProfile("cycling")).isEqualTo("cycling-regular");
    }

    @Test
    void carResolvesToDrivingCar() {
        assertThat(TransportProfileResolver.resolveProfile("Car")).isEqualTo("driving-car");
    }

    @Test
    void vacationResolvesToDrivingCarDefault() {
        assertThat(TransportProfileResolver.resolveProfile("vacation")).isEqualTo("driving-car");
    }

    @Test
    void nullResolvesToDrivingCarDefault() {
        assertThat(TransportProfileResolver.resolveProfile(null)).isEqualTo("driving-car");
    }
}
