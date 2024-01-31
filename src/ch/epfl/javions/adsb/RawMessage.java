package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

/**
 * Represents a "raw" ADS-B message, i.e. whose ME attribute has not yet been analyzed
 *
 * @param timeStampNs (long) : timestamp of the message, in nanoseconds, from a given origin
 * @param bytes       (ByteString) : the bytes of the message
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    /**
     * Length in bytes of ADS-B messages
     */
    public static final int LENGTH = 14;

    /**
     * Constant down link format size
     */
    public static final int DF_SIZE = 17;
    private static final Crc24 CRC_24 = new Crc24(Crc24.GENERATOR);
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    /**
     * Checks if the parameters (timestamp and bytes)
     *
     * @param timeStampNs (long) : timestamp of the message, in nanoseconds, from a given origin
     * @param bytes       (bytes) : the bytes of the message
     */
    public RawMessage {
        Preconditions.checkArgument(bytes.size() == LENGTH && timeStampNs >= 0);
    }

    /**
     * Returns raw ADS-B message
     *
     * @param timeStampNs (long) : timestamp
     * @param bytes       (byte[]) : raw message
     * @return : raw ADS-B message iff the crc24 is equal to 0
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        return (CRC_24.crc(bytes) != 0) ? null : new RawMessage(timeStampNs, new ByteString(bytes));
    }

    /**
     * Returns the size of a message whose first byte is the given one
     *
     * @param byte0 (byte) : first byte of the message
     * @return : the size of a message whose first byte is the given one
     */
    public static int size(byte byte0) {
        return (Bits.extractUInt(byte0, 3, 5) == DF_SIZE) ? LENGTH : 0;
    }

    /**
     * Returns the type code of the message given its payload (ME attribute)
     *
     * @param payload (long) : payload
     * @return : the five leftmost bits of its payload (ME attribute)
     */
    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, 51, 5);
    }

    /**
     * Returns the format of the message
     *
     * @return : the DF attribute stored in the message's first byte
     */
    public int downLinkFormat() {
        return Bits.extractUInt(bytes.byteAt(0), 3, 5);
    }

    /**
     * Returns the ICAO address of the sender of the message
     *
     * @return : ICAO address of the sender of the message
     */
    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HEX_FORMAT.toHexDigits(bytes.bytesInRange(1, 4), 6));
    }

    /**
     * Returns the message's ME attribute — its "payload"
     *
     * @return : the massage's ME attribute
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * Returns the type code of the message
     *
     * @return : the five leftmost bits of it's ME attribute
     */
    public int typeCode() {
        return typeCode(payload());
    }

}


