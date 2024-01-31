package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Represents an OACI address
 *
 * @param string (String) : Contains the textual representation of the OACI address
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record IcaoAddress(String string) {
    /**
     * A regular expression for the OACI address of type pattern
     */
    public static final Pattern PATTERN = Pattern.compile("[0-9A-F]{6}");

    /**
     * Validates the string that is passed as a parameter
     *
     * @param string (String) : Contains the textual representation of the OACI address
     * @throws IllegalArgumentException if the OACI address in not valid
     */
    public IcaoAddress {
        Preconditions.checkArgument(PATTERN.matcher(string).matches());
    }

}

