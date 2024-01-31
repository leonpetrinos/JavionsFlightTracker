package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents what is called the "call sign" of an aircraft
 * The call sign is another type of constrained chain
 *
 * @param string (String) : Contains the textual representation of the call sign
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record CallSign(String string) {
    /**
     * A regular expression for the call sign of type pattern
     */
    public static Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Validates the string that is passed as a parameter
     *
     * @param string (String) : Contains the textual representation of the call sign number
     * @throws IllegalArgumentException if the call sign number in not valid
     */
    public CallSign {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }
}
