package ch.epfl.javions;

/**
 * Represents a 24-bit CRC (cyclic redundancy check) calculator. This means that this class provides the methods to
 * check if a message received from an aircraft is valid or corrupted.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Crc24 {
    /**
     * 24 bit generator used for the purpose of this project
     */
    public static final int GENERATOR = 0xFFF409;
    private final int[] table;
    private static final int CRC_BITS = 24;
    private static final int TOTAL_CRC_VALUES = 256;

    /**
     * Assigns a table to a class attribute that is built using the buildTable function with the generator
     *
     * @param generator (int) : given generator
     */
    public Crc24(int generator) {
        this.table = buildTable(generator);
    }

    /**
     * Algorithm to calculate the CRC operating byte by byte (more optimal version of the CRC algorithm)
     *
     * @param bytes (byte[]) : message that is received from the aircraft
     * @return the CRC
     */
    public int crc(byte[] bytes) {
        int crc = 0;

        for (byte aByte : bytes) {
            crc = ((crc << Byte.SIZE) | (Byte.toUnsignedInt(aByte))) ^ table[Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE)];
        }

        for (int i = 0; i < CRC_BITS / Byte.SIZE; ++i) {
            crc = (crc << Byte.SIZE) ^ table[Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE)];
        }

        return Bits.extractUInt(crc, 0, CRC_BITS);
    }

    /**
     * Algorithm to calculate the CRC operating bit by bit
     *
     * @param bytes     (byte[]) : message that is received from the aircraft
     * @param generator (int) : generator
     * @return the CRC
     */
    private static int crc_bitwise(byte[] bytes, int generator) {
        int[] array = {0, generator};
        int crc = 0;

        for (byte aByte : bytes) {
            for (int j = Byte.SIZE - 1; j >= 0; --j) { // We go backwards because extractUInt counts from right to left
                crc = ((crc << 1) |
                        Bits.extractUInt(aByte, j, 1)) ^
                        array[Bits.extractUInt(crc, CRC_BITS - 1, 1)];
            }
        }

        for (int i = 0; i < CRC_BITS; ++i) {
            crc = (crc << 1) ^ array[Bits.extractUInt(crc, CRC_BITS - 1, 1)];
        }

        return Bits.extractUInt(crc, 0, CRC_BITS);
    }

    /**
     * Builds a table that is needed for the CRC optimised algorithm
     *
     * @param generator (int) : generator
     * @return table with 256 entries, each calculated using the crcBitwise algorithm
     */
    private static int[] buildTable(int generator) {
        int[] table = new int[TOTAL_CRC_VALUES];
        for (int i = 0; i < table.length; ++i) {
            table[i] = crc_bitwise(new byte[]{(byte) i}, generator);
        }
        return table;
    }

}
