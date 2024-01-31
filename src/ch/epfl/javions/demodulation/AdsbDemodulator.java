package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an ADS-B message demodulator
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class AdsbDemodulator {

    /**
     * Constant window size corresponding to the size of a message including the preamble
     */
    public static final int WINDOW_SIZE = 1200;
    private static final int SAMPLES_OF_PREAMBLE = 80;
    private final PowerWindow powerWindow;

    /**
     * Creates a power window of size WINDOW_SIZE (1200)
     *
     * @param samplesStream (InputStream) : stream of samples received
     * @throws IOException if an input/output error occurs
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        powerWindow = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * Decodes an ADS-B message from a sample stream
     *
     * @return : the next ADS-B message from the sample stream passed to the constructor, or null if there are none
     * @throws IOException if an input/output error occurs
     */
    public RawMessage nextMessage() throws IOException {
        byte[] rawMessage = new byte[RawMessage.LENGTH];
        int presentSumP, postSumP, previousSumP = 0;

        while (powerWindow.isFull()) {
            presentSumP = pSum(0);
            postSumP = pSum(1);
            // Check if the preamble is valid
            if ((presentSumP >= 2 * vSum(0)) && (presentSumP > postSumP) && (presentSumP > previousSumP)) {
                fillFirstByte(rawMessage);
                // Check if the down link format is correct
                if (RawMessage.size(rawMessage[0]) == RawMessage.LENGTH) {
                    fillLastBytes(rawMessage);
                    RawMessage rM = RawMessage.of((powerWindow.position() * 100), rawMessage);
                    if (rM != null) {
                        powerWindow.advanceBy(WINDOW_SIZE);
                        return rM;
                    }
                }
            }
            powerWindow.advance();
            previousSumP = presentSumP;
        }
        return null;
    }

    /**
     * Calculates the sum of the peaks. i.e. where the carrier should be transmitted
     *
     * @param index (int) : position to start the calculation
     * @return : the sum of "peaks" of power
     */
    private int pSum(int index) {
        return powerWindow.get(index) + powerWindow.get(10 + index) +
                powerWindow.get(35 + index) + powerWindow.get(45 + index);
    }

    /**
     * Calculates the sum of the valleys. i.e. where the carrier shouldn't be transmitted
     *
     * @param index (int) : position to start the calculation
     * @return : the sum of "valleys" of power
     */
    private int vSum(int index) {
        return powerWindow.get(5 + index) + powerWindow.get(15 + index) + powerWindow.get(20 + index) +
                powerWindow.get(25 + index) + powerWindow.get(30 + index) + powerWindow.get(40 + index);
    }

    /**
     * Variable used for helper methods. It is stocked as a attribute of the class for efficiency
     */
    private final byte[] windowBits = new byte[Byte.SIZE];

    /**
     * Fills the windowBits array. Helper method to make the two other helper methods more clear
     *
     * @param index (int) : index to fill from
     */
    private void fillWindowBits(int index) {
        boolean b = powerWindow.get(SAMPLES_OF_PREAMBLE + (10 * index)) < powerWindow.get(85 + (10 * index));
        windowBits[index % 8] = b ? (byte) 0 : (byte) 1;
    }

    /**
     * Fills the first byte of the array with the first eight bits of a valid message.
     * Since the format of the message is in these bits, we use this method to check
     * if the message is valid so that we can persue the decoding
     *
     * @param rawMessage (byte[]) : array containing the decoded message (to be filled)
     */
    private void fillFirstByte(byte[] rawMessage) {
        for (int i = 0; i < Byte.SIZE; ++i) {
            fillWindowBits(i);
        }
        addByte(rawMessage, 0);
    }

    /**
     * Fills the last 13 bytes of the array with the last bits of the message
     *
     * @param rawMessage (byte[]) : array containing the decoded message (to be filled)
     */

    private void fillLastBytes(byte[] rawMessage) {
        int count = 1;
        for (int i = Byte.SIZE; i < RawMessage.LENGTH * Byte.SIZE; ++i) {
            fillWindowBits(i);
            if ((i + 1) % 8 == 0) {
                addByte(rawMessage, count);
                ++count;
            }
        }
    }

    /**
     * Helper method to help fill the bytes with the raw message
     *
     * @param rawMessage (byte[]) : array containing the decoded message (to be filled)
     * @param index      (int) : index of rawMessage array to add byte at
     */
    private void addByte(byte[] rawMessage, int index) {
        for (int j = 0; j < Byte.SIZE; ++j) {
            rawMessage[index] <<= 1;
            rawMessage[index] |= windowBits[j];
        }
    }


}
