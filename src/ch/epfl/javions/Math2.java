package ch.epfl.javions;

/**
 * Contains functions that perform certain mathematical calculations
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Math2 {

    private Math2() {
    }

    /**
     * Limits a value between two bounds
     *
     * @param min (int) : lower bound
     * @param v   (int) : value
     * @param max (int) : upper bound
     * @return either min, max, or v depending on conditions
     * @throws IllegalArgumentException if min > max
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return Math.max(min, Math.min(v, max));
    }

    /**
     * Calculates arcsinh
     *
     * @param x (double) : argument that arcsin takes
     * @return arcsin(x)
     */
    public static double asinh(double x) {
        return Math.log(x + Math.hypot(1, x));
    }

}
