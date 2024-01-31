package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a window of fixed size over a sequence of power samples produced by a power calculator.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class PowerWindow {
    private static final int BATCH_SIZE = 1 << 16;
    private final PowerComputer pc;
    private final int windowSize;
    private int[] evenIndexBatch;
    private int[] oddIndexBatch;
    private long position;
    private int nbSamplesRead;

    /**
     * Reads a first bach of power samples and stocks it in one of two arrays that the window goes over
     *
     * @param stream     (InputStream) : input stream containing octets representing samples received from AirSpy
     * @param windowSize (int) : size of the window
     * @throws IOException              if an input/output error occurs
     * @throws IllegalArgumentException if the window size is not between 0 (inclusive) and 2^16 (exclusive)
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument((windowSize > 0) && (windowSize <= BATCH_SIZE));
        this.windowSize = windowSize;
        this.evenIndexBatch = new int[BATCH_SIZE];
        this.oddIndexBatch = new int[BATCH_SIZE];
        this.pc = new PowerComputer(stream, BATCH_SIZE);
        this.nbSamplesRead = pc.readBatch(evenIndexBatch);
    }

    /**
     * Returns the window size
     *
     * @return : window size
     */
    public int size() {
        return windowSize;
    }

    /**
     * Returns the current position of the window relative to the start of the stream of power values
     *
     * @return : current position of the window relative to the start of the stream of power values
     */
    public long position() {
        return position;
    }

    /**
     * Checks if the window is full
     *
     * @return : true if and only if the window contains as many samples as its size
     */
    public boolean isFull() {
        return (windowSize + position) <= nbSamplesRead;
    }

    /**
     * Returns the power sample at the given index of the window
     *
     * @param i (int) : index of the window
     * @return : power sample at the given index of the window
     * @throws IndexOutOfBoundsException if the index is not between 0 (inclusive) and window size (exclusive)
     */
    public int get(int i) {
        Objects.checkIndex(i, windowSize);
        int simplifiedIndex = (int) (position % BATCH_SIZE) + i;
        return (simplifiedIndex < BATCH_SIZE) ? evenIndexBatch[simplifiedIndex] : oddIndexBatch[simplifiedIndex - BATCH_SIZE];
    }

    /**
     * Advances the window by one sample
     *
     * @throws IOException if an input/output error occurs
     */
    public void advance() throws IOException {
        long maxPosition = position + windowSize - 1;
        // check if the head position of the window is at the end of the batch
        if ((maxPosition) % BATCH_SIZE == BATCH_SIZE - 1) nbSamplesRead += pc.readBatch(oddIndexBatch);
        // check if the tail position of the window is at the start of a new batch
        if ((position + 1) % BATCH_SIZE == 0) {
            int[] temp = evenIndexBatch;
            evenIndexBatch = oddIndexBatch;
            oddIndexBatch = temp;
        }
        ++position;
    }

    /**
     * Advances the window by a given number of samples
     *
     * @param offset (int) : number of samples to advance the window by
     * @throws IOException              if an input/output error occurs
     * @throws IllegalArgumentException if the offset is less than or equal to 0
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset > 0);
        for (int i = 0; i < offset; ++i) {
            advance();
        }
    }

}
