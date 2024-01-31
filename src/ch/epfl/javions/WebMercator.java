package ch.epfl.javions;

/**
 * Contains methods allowing to project geographical coordinates using the WebMercator projection
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class WebMercator {

    private WebMercator() {
    }

    /**
     * Outputs the x-coordinate corresponding to the x in Turns
     *
     * @param zoomLevel (int) : scale of the zoom
     * @param longitude (double) : x in radians
     * @return x-coordinate corresponding to the x in Turns
     */
    public static double x(int zoomLevel, double longitude) {
        double value = Units.convertTo(longitude, Units.Angle.TURN);
        return calculateCoordinate(zoomLevel, value);
    }

    /**
     * Outputs the y-coordinate corresponding to the latitude in Turns
     *
     * @param zoomLevel (int) : scale of the zoom
     * @param latitude  (double) : latitude in radians
     * @return y-coordinate corresponding to the latitude in Turns
     */
    public static double y(int zoomLevel, double latitude) {
        double value = -1 * Units.convertTo(Math2.asinh(Math.tan(latitude)), Units.Angle.TURN);
        return calculateCoordinate(zoomLevel, value);
    }

    /**
     * Calculates coordinates (longitude or latitude) in Turn
     *
     * @param zoomLevel (int) : scale of the zoom
     * @param value     (double) : value that depends on if we find longitude or latitude
     * @return : coordinates in Turn
     */
    private static double calculateCoordinate(int zoomLevel, double value) {
        return Math.scalb(1, 8 + zoomLevel) * (value + 0.5);
    }

}
