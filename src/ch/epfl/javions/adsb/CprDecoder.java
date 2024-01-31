package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * Represents a position decoder (CPR, i.e. , compact position reporting)
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class CprDecoder {
    private final static int EVEN_LATITUDE_ZONES = 60;
    private final static int ODD_LATITUDE_ZONES = 59;
    private static final double[] PHI = new double[2];
    private static final double[] LAMBDA = new double[2];


    private CprDecoder() {
    }

    /**
     * Calculates the geographic position of the aircraft
     *
     * @param x0         (double) : normalized longitude of the "even" message (between 0 and 1)
     * @param y0         (double) : normalized latitude of the "even" message (between 0 and 1)
     * @param x1         (double) : normalized longitude of the "odd" message (between 0 and 1)
     * @param y1         (double) : normalized latitude of the "odd" message (between 0 and 1)
     * @param mostRecent (int) : equal to 0 if the first message is even and 1 if the first message is odd
     * @return : geographic position corresponding to the local normalised positions (longitudes and latitudes)
     * @throws IllegalArgumentException if mostRecent is not equal to 0 or 1;
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        // First we calculate number of zones of latitude in each group (even = 0, odd = 1)
        double zPhi = Math.rint((y0 * ODD_LATITUDE_ZONES) - (y1 * EVEN_LATITUDE_ZONES));
        PHI[0] = calculateAngles(zPhi, EVEN_LATITUDE_ZONES, y0);
        PHI[1] = calculateAngles(zPhi, ODD_LATITUDE_ZONES, y1);

        // Now, we calculate the number of zones of longitude,
        // and we check that they are equal first for both the even and odd latitude
        double evenLongitudeZonesFromPhi0 = calculateEvenLongitudeZones(PHI[0]);
        double evenLongitudeZonesFromPhi1 = calculateEvenLongitudeZones(PHI[1]);


        if (evenLongitudeZonesFromPhi0 == evenLongitudeZonesFromPhi1) {

            PHI[0] = Math.rint(Units.convert(centerTurns(PHI[0]), Units.Angle.TURN, Units.Angle.T32));
            PHI[1] = Math.rint(Units.convert(centerTurns(PHI[1]), Units.Angle.TURN, Units.Angle.T32));

            if (!(GeoPos.isValidLatitudeT32((int) PHI[0]) && GeoPos.isValidLatitudeT32((int) PHI[1])))
                return null;


            // Specific case if the number of even longitude zones are 1
            if (evenLongitudeZonesFromPhi0 == 1) {
                x0 = Math.rint(Units.convert(centerTurns(x0), Units.Angle.TURN, Units.Angle.T32));
                x1 = Math.rint(Units.convert(centerTurns(x1), Units.Angle.TURN, Units.Angle.T32));
                return (mostRecent == 0) ? new GeoPos((int) x0, (int) PHI[0]) : new GeoPos((int) x1, (int) PHI[1]);
            }

            double oddLongitudeZones = evenLongitudeZonesFromPhi0 - 1;

            // Calculate number of zones of longitude in each group (even = 0, odd = 1)
            double zLambda = Math.rint((x0 * oddLongitudeZones) - (x1 * evenLongitudeZonesFromPhi0));
            LAMBDA[0] = calculateAngles(zLambda, evenLongitudeZonesFromPhi0, x0);
            LAMBDA[1] = calculateAngles(zLambda, oddLongitudeZones, x1);

            LAMBDA[0] = Math.rint(Units.convert(centerTurns(LAMBDA[0]), Units.Angle.TURN, Units.Angle.T32));
            LAMBDA[1] = Math.rint(Units.convert(centerTurns(LAMBDA[1]), Units.Angle.TURN, Units.Angle.T32));

            return (mostRecent == 0) ?
                    new GeoPos((int) LAMBDA[0], (int) PHI[0]) :
                    new GeoPos((int) LAMBDA[1], (int) PHI[1]);

        }
        return null;
    }

    /**
     * Centers an angle between -90° and 90° (int turns)
     *
     * @param angle (double) : angle in turns
     * @return : angle that is centered
     */
    private static double centerTurns(double angle) {
        return (angle >= 0.5) ? angle - 1 : angle;
    }

    /**
     * Applies a formula to a latitude to facilitate the calculation of the number of longitude zones
     *
     * @param angle (double) : latitude in turns
     * @return : the result of this formula
     */
    private static double cosineFormula(double angle) {
        double angleRad = Units.convertFrom(angle, Units.Angle.TURN);
        return Math.acos(1 - ((1 - Math.cos(2 * Math.PI * (1.0 / EVEN_LATITUDE_ZONES))) /
                (Math.cos(angleRad) * Math.cos(angleRad))));
    }

    /**
     * Calculates number of even longitude zones
     *
     * @param angle (double) : latitude in turns
     * @return : number of even longitude zones
     */
    private static double calculateEvenLongitudeZones(double angle) {
        double A = cosineFormula(angle);
        return Double.isNaN(A) ? 1 : Math.floor((2 * Math.PI) / A);
    }


    /**
     * Calculates the latitude or longitude
     *
     * @param z        (double) : value depending on if we are operating on longitude or latitude
     * @param zones    (double) : zones of latitude or longitude (even or odd)
     * @param position (double) : either latitude or longitude (even or odd)
     * @return : the latitude or longitude of a message
     */
    private static double calculateAngles(double z, double zones, double position) {
        double nbZones = (z < 0) ? z + zones : z;
        return (1.0 / zones) * (nbZones + position);
    }


}
