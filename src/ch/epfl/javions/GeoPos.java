package ch.epfl.javions;

/**
 * Represents geographical coordinates
 *
 * @param longitudeT32 (int) : x in T32
 * @param latitudeT32  (int) : latitude in T32
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

    private static final int MAX_LATITUDE_T32 = 1 << 30;
    private static final int MIN_LATITUDE_T32 = -MAX_LATITUDE_T32;

    /**
     * Checks if the latitude is valid and throws an exception if not
     *
     * @param longitudeT32 (int) : x in T32
     * @param latitudeT32  (int) : latitude in T32
     * @throws IllegalArgumentException if latitude is out of bounds (not between -2^30 and 2^30 included)
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Checks if the latitude is valid (between -2^30 (90°) and 2^30 (90°) included)
     *
     * @param latitudeT32 (int) : latitude in T32
     * @return boolean that shows if latitude is in bounds (between -2^30 (90°) and 2^30(90°) included)
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return MIN_LATITUDE_T32 <= latitudeT32 && latitudeT32 <= MAX_LATITUDE_T32;
    }

    /**
     * Converts x from T32 to radians
     *
     * @return x in radians
     */
    public double longitude() {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * Converts latitude from T32 to radians
     *
     * @return latitude in radians
     */
    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     * Outputs longitude and latitude in degrees as a String
     *
     * @return (longitudeInDegrees, latitudeInDegrees)
     */
    @Override
    public String toString() {
        double longitudeInDegrees = Units.convertTo(longitude(), Units.Angle.DEGREE);
        double latitudeInDegrees = Units.convertTo(latitude(), Units.Angle.DEGREE);
        return ("(" + longitudeInDegrees + "°, " + latitudeInDegrees + "°)");
    }


}
