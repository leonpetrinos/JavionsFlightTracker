package ch.epfl.javions;

/**
 * Represents a class to throw illegal argument exceptions
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Preconditions {

    private Preconditions() {
    }

    /**
     * Checks if the argument is false
     *
     * @param shouldBeTrue (boolean) : argument to check
     * @throws IllegalArgumentException if parameter is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }

}
