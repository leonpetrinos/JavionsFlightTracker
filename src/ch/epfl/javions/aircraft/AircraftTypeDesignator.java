package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents a type designator number
 * @param string (String) : Contains the textual representation of the type designator number
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */

public record AircraftTypeDesignator(String string) {
    /**
     * A regular expression for the type designator number of type pattern
     */
    public static final Pattern PATTERN = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * Validates the string that is passed as a parameter
     * @param string (String) : Contains the textual representation of the type designator number
     * @throws IllegalArgumentException if the type designator number in not valid
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(PATTERN.matcher(string).matches() || string.isEmpty());
    }

}

