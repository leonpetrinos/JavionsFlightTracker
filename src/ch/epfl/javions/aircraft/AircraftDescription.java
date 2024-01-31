package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents the aircraft description : a three-letter code giving the type of the aircraft, its number of engines
   and its type of propulsion
 * @param string (String) : Contains the textual representation of the aircraft description
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */

public record AircraftDescription(String string) {
    /**
     * A regular expression for the aircraft description of type pattern
     */
    public static final Pattern PATTERN = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * Validates the string that is passed as a parameter
     * @param string (String) : Contains the textual representation of the aircraft description
     * @throws IllegalArgumentException if the aircraft description in not valid
     */
    public AircraftDescription {
        Preconditions.checkArgument(PATTERN.matcher(string).matches() || string.isEmpty());
    }

}

