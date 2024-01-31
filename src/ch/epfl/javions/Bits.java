package ch.epfl.javions;

import java.util.Objects;

/**
 * Contains methods which allow you to extract a subset of the 64 bits from a value of type long
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Bits {

    private Bits() {
    }

    /**
     * Extracts from a sequence of bits of type long a subset of a given size and start
     * It is not signed
     *
     * @param value (long) : 64 bit value
     * @param start (int) : index where the extraction starts (index 0 is at the right)
     * @param size  (int) : size of the subset
     * @return sequence of bits extracted from the value
     * @throws IllegalArgumentException  if size is negative or greater than or equal to 32
     * @throws IndexOutOfBoundsException if the range described by start and size is not completely between
     *                                   0 (inclusive) and 64 (exclusive)
     */
    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(size > 0 && size < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Long.SIZE);
        int mask = (1 << size) - 1; // There are "size" 1's on the right and all zeros on the left
        return (int) (value >>> start) & mask; // Here we match the subset we want to extract with the mask
    }

    /**
     * Extracts one bit (either 0 or 1) from a value at a given index and returns true if its value is one
     *
     * @param value (long) : 64 bit value
     * @param index (int) : index we extract the bit from
     * @return the extracted bit (either 0 or 1)
     * @throws IndexOutOfBoundsException if index not between 0 (inclusive) and 64 (exclusive)
     */
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        int extractedBit = ((int) (value >>> index)) & 1; // The & operator allows us to narrow the value to 0 or 1
        return extractedBit == 1;
    }


}
