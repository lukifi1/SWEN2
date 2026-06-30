package at.fhtw.tourplanner.server.business.security;

import at.fhtw.tourplanner.server.config.TourPlannerProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protects token issuance and validation in {@link JwtService}, the backbone of the
 * stateless authentication scheme. A round-trip must recover the subject username,
 * and any tampered/garbage token or a token signed with a different key must be
 * rejected (returns {@link Optional#empty()}) so that forged credentials never grant
 * access.
 */
class JwtServiceTest {

    private static JwtService serviceWithSecret(String secret) {
        TourPlannerProperties properties = new TourPlannerProperties();
        properties.getJwt().setSecret(secret);
        properties.getJwt().setExpirationMs(3_600_000L);
        return new JwtService(properties);
    }

    @Test
    void generatedTokenRoundTripsToTheSameUsername() {
        JwtService service = serviceWithSecret("this-is-a-very-long-test-secret-key-32+chars");

        String token = service.generateToken("alice");

        assertThat(service.extractUsername(token)).contains("alice");
    }

    @Test
    void garbageTokenYieldsEmpty() {
        JwtService service = serviceWithSecret("this-is-a-very-long-test-secret-key-32+chars");

        assertThat(service.extractUsername("not-a-real-jwt")).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        JwtService issuer = serviceWithSecret("this-is-a-very-long-test-secret-key-32+chars");
        JwtService verifier = serviceWithSecret("a-completely-different-secret-key-also-32chars");

        String token = issuer.generateToken("alice");

        assertThat(verifier.extractUsername(token)).isEmpty();
    }
}
