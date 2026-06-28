package at.fhtw.tourplanner.server.business.computation;

/**
 * Human readable labels for the computed attributes. These are included in the
 * full-text search index so users can search by terms like "child friendly"
 * or "popular", i.e. the search also considers the computed values.
 */
public final class ComputedAttributeLabels {

    private ComputedAttributeLabels() {
    }

    public static String popularityLabel(Integer popularity) {
        int p = popularity == null ? 0 : popularity;
        if (p == 0) return "unpopular no logs";
        if (p >= 5) return "very popular";
        if (p >= 2) return "popular";
        return "rarely logged";
    }

    public static String childFriendlinessLabel(Double score) {
        double s = score == null ? 0.0 : score;
        if (s >= 66) return "child friendly kid friendly family friendly easy for children";
        if (s >= 33) return "moderately child friendly";
        return "not child friendly challenging for children difficult";
    }
}
