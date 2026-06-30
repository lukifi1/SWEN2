package at.fhtw.tourplanner.server.business.importexport;

/**
 * Strategy pattern: abstracts the serialization format used for importing and
 * exporting tour data. New formats (XML, CSV, ...) can be added by providing
 * another implementation without touching the {@code ImportExportService}.
 */
public interface ImportExportStrategy {

    /** Identifier of the format, e.g. "json". */
    String formatName();

    /** File extension (without dot) for exported files. */
    String fileExtension();

    /** MIME type for the HTTP response. */
    String contentType();

    /** Serializes the given data into the target format. */
    byte[] export(TourExportFile data);

    /** Parses raw bytes in the target format back into the data model. */
    TourExportFile parse(byte[] data);
}
