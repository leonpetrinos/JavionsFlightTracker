package ch.epfl.javions;

/**
 * A collection of units for different measurements
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Units {
    /**
     * SI unit for centi
     */
    public static final double CENTI = 1e-2;
    /**
     * SI unit for kilo
     */
    public static final double KILO = 1e3;

    private Units() {
    }

    /**
     * Converts a value's units
     *
     * @param value    (double) : given number
     * @param fromUnit (double) : value's units
     * @param toUnit   (double) : unit we change to
     * @return the value with converted units
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * Converts a value to the standard unit
     *
     * @param value    (double) : given number
     * @param fromUnit (double) : value's units
     * @return value in standard unit
     */
    public static double convertFrom(double value, double fromUnit) {
        return convert(value, fromUnit, 1);
    }

    /**
     * Converts a value from the standard unit to another unit
     *
     * @param value  (double) : given number
     * @param toUnit (double) : unit we are converting the value to
     * @return value in the toUnit
     */
    public static double convertTo(double value, double toUnit) {
        return convert(value, 1, toUnit);
    }

    /**
     * Contains definitions of units of angle
     */
    public static class Angle {
        /**
         * Base unit for angle
         */
        public static final double RADIAN = 1;
        /**
         * 1 turn : 2π x RADIAN
         */
        public static final double TURN = 2 * Math.PI * RADIAN;
        /**
         * 1 degree : TURN/360
         */
        public static final double DEGREE = TURN / 360;
        /**
         * 1 T32 : TURN/2^32
         */
        public static final double T32 = TURN / Math.scalb(1, 32);
    }

    /**
     * Contains definitions of units of length
     */
    public static class Length {
        /**
         * Base unit for length
         */
        public static final double METER = 1;
        /**
         * 1 centimeter : 10^-2 x METER
         */
        public static final double CENTIMETER = CENTI * METER;
        /**
         * 1 kilometer : 10^3 x METER
         */
        public static final double KILOMETER = KILO * METER;
        /**
         * 1 inch : 2.54 x CENTIMETER
         */
        public static final double INCH = 2.54 * CENTIMETER;
        /**
         * 1 foot : 12 x INCH
         */
        public static final double FOOT = 12 * INCH;
        /**
         * 1 nautical mile : 1852 x METER
         */
        public static final double NAUTICAL_MILE = 1852 * METER;

    }

    /**
     * Contains definitions of units of time
     */
    public static class Time {
        /**
         * Base unit for time
         */
        public static final double SECOND = 1;
        /**
         * 1 minute : 60 seconds
         */
        public static final double MINUTE = 60 * SECOND;
        /**
         * 1 hour : 60 minutes
         */
        public static final double HOUR = 60 * MINUTE;

    }

    /**
     * Contains definitions of units of speed
     */
    public static class Speed {
        /**
         * nautical miles per hour
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        /**
         * kilometers per hour
         */
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;

    }


}
