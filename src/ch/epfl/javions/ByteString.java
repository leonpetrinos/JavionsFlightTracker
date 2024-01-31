package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * It has similar instances to the type byte[] with two differences:
 * 1) It is not possible to change the octets that an instance contains once created
 * 2) Its octets are unsigned
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class ByteString {
    private final byte[] bytes;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    /**
     * Assigns a cloned version of the parameter to the private variable bytes
     *
     * @param bytes (byte[]) : Array of bytes
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /**
     * Returns the chain of octets (bytes) from a chain in hexadecimal form
     *
     * @param hexString (String) : Hexadecimal number
     * @return chain of octets (bytes) from a chain in hexadecimal form
     * @throws IllegalArgumentException if the given string is not of even length, or if it contains a character that
     *                                  is not a hexadecimal digit
     */
    public static ByteString ofHexadecimalString(String hexString) {
        return new ByteString(HEX_FORMAT.parseHex(hexString));
    }

    /**
     * Length of the bytes array
     *
     * @return length of the bytes array
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Returns the byte at a given index of the bytes array (not signed)
     *
     * @param index (int) : index of the byte in the array
     * @return byte at bytes[index] as an int (not signed)
     * @throws IndexOutOfBoundsException if index is greater than the length of the bytes array - 1
     */
    public int byteAt(int index) {
        return Byte.toUnsignedInt(bytes[index]);
    }

    /**
     * Returns the bytes between the indexes fromIndex (inclusive) and toIndex (excluded) as a value of type long, with
     * the byte at index toIndex - 1 being the least significant byte of the result
     *
     * @param fromIndex (int) : starting index (inclusive)
     * @param toIndex   (int) : ending index (exclusive)
     * @return a value of type long which contains the extracted bytes from the bytes array
     * @throws IndexOutOfBoundsException if the range described by fromIndex and toIndex is not completely between 0 and
     *                                   the size of the string,
     * @throws IllegalArgumentException  if the difference between toIndex and fromIndex is not strictly less than the
     *                                   number of bytes contained in a long type value.
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);

        int size = toIndex - fromIndex;
        long bytes = 0;
        int j = 1;

        for (int i = fromIndex; i < toIndex; ++i) {
            bytes |= (long) byteAt(i) << (Byte.SIZE * (size - j));
            ++j;
        }

        return bytes;
    }

    /**
     * Tests if two instances are of the class ByteString and are equal
     *
     * @param obj (Object) : any object
     * @return boolean value that is true if two instances are equal
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ByteString byteString) && (Arrays.equals(byteString.bytes, this.bytes));
    }

    /**
     * Determines the hash code for the bytes array using the method from the Arrays class
     *
     * @return hash code for bytes array
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    /**
     * Redefinition of toString method from object that outputs a representation of the bytes in
     * hexadecimal, with each byte occupying two characters.
     *
     * @return a String of the bytes in hexadecimal with each byte occupying two characters
     * @throws IndexOutOfBoundsException if array out of bounds (formatHex function does this)
     */
    @Override
    public String toString() {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }


}
