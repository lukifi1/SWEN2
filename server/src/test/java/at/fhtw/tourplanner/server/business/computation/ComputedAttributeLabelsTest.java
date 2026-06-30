package at.fhtw.tourplanner.server.business.computation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protects the human-readable labels in {@link ComputedAttributeLabels}. These
 * labels are folded into the full-text search index, so users can search by terms
 * like "child friendly" or "popular". The exact boundary buckets (0 / 2 / 5 for
 * popularity; 33 / 66 for child-friendliness) determine which tours match those
 * queries and must not drift.
 */
class ComputedAttributeLabelsTest {

    @Test
    void popularityLabelForNoLogs() {
        assertThat(ComputedAttributeLabels.popularityLabel(0)).isEqualTo("unpopular no logs");
    }

    @Test
    void popularityLabelForNullDefaultsToNoLogs() {
        assertThat(ComputedAttributeLabels.popularityLabel(null)).isEqualTo("unpopular no logs");
    }

    @Test
    void popularityLabelForRarelyLogged() {
        assertThat(ComputedAttributeLabels.popularityLabel(1)).isEqualTo("rarely logged");
    }

    @Test
    void popularityLabelForPopular() {
        assertThat(ComputedAttributeLabels.popularityLabel(2)).isEqualTo("popular");
    }

    @Test
    void popularityLabelForVeryPopular() {
        assertThat(ComputedAttributeLabels.popularityLabel(5)).isEqualTo("very popular");
    }

    @Test
    void childFriendlinessLabelForHighScoreIsChildFriendly() {
        assertThat(ComputedAttributeLabels.childFriendlinessLabel(80.0))
                .contains("child friendly");
    }

    @Test
    void childFriendlinessLabelForMidScoreIsModerate() {
        assertThat(ComputedAttributeLabels.childFriendlinessLabel(50.0))
                .isEqualTo("moderately child friendly");
    }

    @Test
    void childFriendlinessLabelForLowScoreIsNotChildFriendly() {
        assertThat(ComputedAttributeLabels.childFriendlinessLabel(10.0))
                .contains("not child friendly");
    }
}
