package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents the registration number
 * @param string (String) : Contains the textual representation of the registration number
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */

public record AircraftRegistration(String string) {
    /**
     * A regular expression for the registration number of type Pattern
     */
    public static final Pattern PATTERN = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Validates the string that is passed as a parameter
     * @param string (String) : Contains the textual representation of the registration number
     * @throws IllegalArgumentException if the registration number in not valid
     */
    public AircraftRegistration {
        Preconditions.checkArgument(PATTERN.matcher(string).matches());
    }

}

