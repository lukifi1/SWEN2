package at.fhtw.tourplanner.server.business.importexport;

import at.fhtw.tourplanner.server.business.exceptions.ImportExportException;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GPX implementation of {@link ImportExportStrategy}.
 * Tour metadata (from/to, distance, logs, …) is stored in a custom
 * {@code <extensions>} namespace so the file is still valid GPX 1.1 and
 * compatible with standard GPS tools that ignore unknown extensions.
 * Route geometry is stored as standard {@code <trkpt>} elements.
 *
 * Marked {@code @Primary} so it replaces the JSON strategy as the default.
 */
@Primary
@Component
public class GpxImportExportStrategy implements ImportExportStrategy {

    private static final String GPX_NS = "http://www.topografix.com/GPX/1/1";
    private static final String TP_NS  = "http://tourplanner.fhtw.at/gpx/1";
    private static final String VERSION = "1.0";

    @Override public String formatName()    { return "gpx"; }
    @Override public String fileExtension() { return "gpx"; }
    @Override public String contentType()   { return "application/gpx+xml"; }

    // -------------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------------

    @Override
    public byte[] export(TourExportFile data) {
        try {
            DocumentBuilder builder = newDocumentBuilder();
            Document doc = builder.newDocument();

            Element gpx = doc.createElementNS(GPX_NS, "gpx");
            gpx.setAttribute("version", "1.1");
            gpx.setAttribute("creator", "TourPlanner");
            gpx.setAttribute("xmlns:tp", TP_NS);
            doc.appendChild(gpx);

            Element metadata = doc.createElementNS(GPX_NS, "metadata");
            appendText(doc, metadata, "name", "TourPlanner Export v" + VERSION);
            gpx.appendChild(metadata);

            for (TourExportFile.TourExport tour : data.tours()) {
                gpx.appendChild(buildTrack(doc, tour));
            }

            return toBytes(doc);
        } catch (Exception ex) {
            throw new ImportExportException("Failed to serialise tour data to GPX.", ex);
        }
    }

    private Element buildTrack(Document doc, TourExportFile.TourExport tour) {
        Element trk = doc.createElementNS(GPX_NS, "trk");
        appendText(doc, trk, "name",  nvl(tour.name()));
        appendText(doc, trk, "desc",  nvl(tour.description()));
        appendText(doc, trk, "type",  nvl(tour.transportType()));

        // TourPlanner-specific extensions
        Element ext = doc.createElementNS(GPX_NS, "extensions");
        appendTpText(doc, ext, "from",          nvl(tour.fromLocation()));
        appendTpText(doc, ext, "to",            nvl(tour.toLocation()));
        appendTpText(doc, ext, "distance",      tour.distance()      != null ? tour.distance().toString()      : "");
        appendTpText(doc, ext, "estimatedTime", tour.estimatedTime() != null ? tour.estimatedTime().toString() : "");
        appendTpText(doc, ext, "imagePath",     nvl(tour.imagePath()));

        if (tour.logs() != null && !tour.logs().isEmpty()) {
            Element logsEl = doc.createElementNS(TP_NS, "tp:logs");
            for (TourExportFile.TourLogExport log : tour.logs()) {
                logsEl.appendChild(buildLogElement(doc, log));
            }
            ext.appendChild(logsEl);
        }
        trk.appendChild(ext);

        // Route geometry as standard trkpt elements
        List<double[]> points = parseGeometry(tour.routeGeometry());
        if (!points.isEmpty()) {
            Element seg = doc.createElementNS(GPX_NS, "trkseg");
            for (double[] pt : points) {
                Element trkpt = doc.createElementNS(GPX_NS, "trkpt");
                trkpt.setAttribute("lat", String.valueOf(pt[0]));
                trkpt.setAttribute("lon", String.valueOf(pt[1]));
                seg.appendChild(trkpt);
            }
            trk.appendChild(seg);
        }
        return trk;
    }

    private Element buildLogElement(Document doc, TourExportFile.TourLogExport log) {
        Element logEl = doc.createElementNS(TP_NS, "tp:log");
        appendTpText(doc, logEl, "dateTime",      log.dateTime()      != null ? log.dateTime().toString() : "");
        appendTpText(doc, logEl, "comment",       nvl(log.comment()));
        appendTpText(doc, logEl, "difficulty",    log.difficulty()    != null ? log.difficulty().toString()    : "");
        appendTpText(doc, logEl, "totalDistance", log.totalDistance() != null ? log.totalDistance().toString() : "");
        appendTpText(doc, logEl, "totalTime",     log.totalTime()     != null ? log.totalTime().toString()     : "");
        appendTpText(doc, logEl, "rating",        log.rating()        != null ? log.rating().toString()        : "");
        return logEl;
    }

    // -------------------------------------------------------------------------
    // Import
    // -------------------------------------------------------------------------

    @Override
    public TourExportFile parse(byte[] data) {
        try {
            DocumentBuilder builder = newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(data));
            doc.getDocumentElement().normalize();

            NodeList trkList = doc.getElementsByTagNameNS(GPX_NS, "trk");
            // fallback: files written without explicit namespace
            if (trkList.getLength() == 0) {
                trkList = doc.getElementsByTagName("trk");
            }

            List<TourExportFile.TourExport> tours = new ArrayList<>();
            for (int i = 0; i < trkList.getLength(); i++) {
                tours.add(parseTrack((Element) trkList.item(i)));
            }
            return new TourExportFile(VERSION, tours);
        } catch (SAXException | IOException ex) {
            throw new ImportExportException("The uploaded file is not valid GPX.", ex);
        } catch (ParserConfigurationException ex) {
            throw new ImportExportException("XML parser configuration error.", ex);
        }
    }

    private TourExportFile.TourExport parseTrack(Element trk) {
        String name         = firstText(trk, "name");
        String description  = firstText(trk, "desc");
        String transportType = firstText(trk, "type");

        // Extensions
        String from          = firstTpText(trk, "from");
        String to            = firstTpText(trk, "to");
        Double distance      = parseDouble(firstTpText(trk, "distance"));
        Double estimatedTime = parseDouble(firstTpText(trk, "estimatedTime"));
        String imagePath     = firstTpText(trk, "imagePath");

        List<TrackPoint> points = trackPoints(trk);

        if (from == null && !points.isEmpty()) {
            from = formatLocation(points.getFirst());
        }
        if (to == null && !points.isEmpty()) {
            to = formatLocation(points.getLast());
        }
        if (distance == null) {
            distance = calculateDistanceKm(points);
        }
        if (estimatedTime == null) {
            estimatedTime = calculateDurationHours(points);
        }

        // Logs
        List<TourExportFile.TourLogExport> logs = new ArrayList<>();
        NodeList logNodes = trk.getElementsByTagNameNS(TP_NS, "log");
        if (logNodes.getLength() == 0) {
            logNodes = trk.getElementsByTagName("tp:log");
        }
        for (int i = 0; i < logNodes.getLength(); i++) {
            logs.add(parseLog((Element) logNodes.item(i)));
        }

        // Route geometry from trkpt
        String geometry = null;
        if (!points.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < points.size(); i++) {
                TrackPoint pt = points.get(i);
                if (i > 0) sb.append(',');
                sb.append('[').append(pt.lat()).append(',')
                              .append(pt.lon()).append(']');
            }
            geometry = sb.append(']').toString();
        }

        return new TourExportFile.TourExport(
                defaultIfBlank(name, "Imported GPX Tour"),
                description,
                defaultIfBlank(from, "Unknown start"),
                defaultIfBlank(to, "Unknown destination"),
                defaultIfBlank(transportType, "Hiking"),
                distance, estimatedTime, geometry, imagePath, logs);
    }

    private TourExportFile.TourLogExport parseLog(Element el) {
        String dt = firstTpText(el, "dateTime");
        return new TourExportFile.TourLogExport(
                dt != null && !dt.isBlank() ? LocalDateTime.parse(dt) : null,
                firstTpText(el, "comment"),
                parseInt(firstTpText(el, "difficulty")),
                parseDouble(firstTpText(el, "totalDistance")),
                parseDouble(firstTpText(el, "totalTime")),
                parseInt(firstTpText(el, "rating")));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void appendText(Document doc, Element parent, String localName, String value) {
        Element el = doc.createElementNS(GPX_NS, localName);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private void appendTpText(Document doc, Element parent, String localName, String value) {
        Element el = doc.createElementNS(TP_NS, "tp:" + localName);
        el.setTextContent(value != null ? value : "");
        parent.appendChild(el);
    }

    private String firstText(Element parent, String localName) {
        NodeList nl = parent.getElementsByTagNameNS(GPX_NS, localName);
        if (nl.getLength() == 0) nl = parent.getElementsByTagName(localName);
        if (nl.getLength() == 0) return null;
        // Only pick direct children to avoid descending into nested elements
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getParentNode() == parent) {
                return nl.item(i).getTextContent().trim();
            }
        }
        return nl.item(0).getTextContent().trim();
    }

    private String firstTpText(Element parent, String localName) {
        NodeList nl = parent.getElementsByTagNameNS(TP_NS, localName);
        if (nl.getLength() == 0) nl = parent.getElementsByTagName("tp:" + localName);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent().trim();
    }

    private List<TrackPoint> trackPoints(Element trk) {
        NodeList trkpts = trk.getElementsByTagNameNS(GPX_NS, "trkpt");
        if (trkpts.getLength() == 0) trkpts = trk.getElementsByTagName("trkpt");

        List<TrackPoint> points = new ArrayList<>();
        for (int i = 0; i < trkpts.getLength(); i++) {
            Element pt = (Element) trkpts.item(i);
            Double lat = parseDouble(pt.getAttribute("lat"));
            Double lon = parseDouble(pt.getAttribute("lon"));
            if (lat == null || lon == null) {
                continue;
            }
            points.add(new TrackPoint(lat, lon, parseInstant(firstText(pt, "time"))));
        }
        return points;
    }

    private String formatLocation(TrackPoint point) {
        return String.format(Locale.ROOT, "%.6f, %.6f", point.lat(), point.lon());
    }

    private Double calculateDistanceKm(List<TrackPoint> points) {
        if (points.size() < 2) return 0.0;
        double meters = 0.0;
        for (int i = 1; i < points.size(); i++) {
            meters += haversineMeters(points.get(i - 1), points.get(i));
        }
        return Math.round((meters / 1000.0) * 100.0) / 100.0;
    }

    private double haversineMeters(TrackPoint a, TrackPoint b) {
        double earthRadiusMeters = 6_371_000.0;
        double lat1 = Math.toRadians(a.lat());
        double lat2 = Math.toRadians(b.lat());
        double deltaLat = Math.toRadians(b.lat() - a.lat());
        double deltaLon = Math.toRadians(b.lon() - a.lon());

        double h = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return earthRadiusMeters * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    private Double calculateDurationHours(List<TrackPoint> points) {
        Instant start = null;
        Instant end = null;
        for (TrackPoint point : points) {
            if (point.time() == null) {
                continue;
            }
            if (start == null) {
                start = point.time();
            }
            end = point.time();
        }
        if (start == null || end == null || end.isBefore(start)) {
            return 0.0;
        }
        double hours = Duration.between(start, end).toSeconds() / 3600.0;
        return Math.round(hours * 100.0) / 100.0;
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private List<double[]> parseGeometry(String json) {
        List<double[]> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;
        // format: [[lat,lon],[lat,lon],...]
        String inner = json.trim().replaceAll("^\\[|]$", "");
        for (String pair : inner.split("],\\s*\\[")) {
            String[] parts = pair.replaceAll("[\\[\\]]", "").split(",");
            if (parts.length >= 2) {
                try {
                    result.add(new double[]{
                            Double.parseDouble(parts[0].trim()),
                            Double.parseDouble(parts[1].trim())});
                } catch (NumberFormatException ignored) { /* skip malformed */ }
            }
        }
        return result;
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record TrackPoint(double lat, double lon, Instant time) {}

    private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // harden against XXE attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder();
    }

    private byte[] toBytes(Document doc) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        return out.toByteArray();
    }
}
