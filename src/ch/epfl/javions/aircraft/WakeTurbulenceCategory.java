package ch.epfl.javions.aircraft;

/**
 * Represents the wake turbulence category of an aircraft. It contains four values which are in order
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * Used to convert String values from the database into items of the enumerated type
     *
     * @param s (String) : String value from the database
     * @return one of the items of the enumerated type depending on the String parameter
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }

}
