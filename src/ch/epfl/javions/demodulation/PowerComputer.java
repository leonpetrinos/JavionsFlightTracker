package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Calculates the samples of power from a signal
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class PowerComputer {

    private final int batchSize;
    private final SamplesDecoder samplesDecoder;
    private final int[] circularArray;
    private final short[] samples;
    private static final int SIZE = 8;

    /**
     * Creates a power calculator
     *
     * @param stream    (InputStream) : input stream containing octets representing samples received from AirSpy
     * @param batchSize (int) : given size of batch
     * @throws IllegalArgumentException if the batch size is not strictly greater than 0 or not a multiple of 8
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument((batchSize % SIZE == 0) && (batchSize > 0));
        this.batchSize = batchSize;
        this.samplesDecoder = new SamplesDecoder(stream, batchSize * 2);
        this.circularArray = new int[SIZE];
        this.samples = new short[2 * batchSize];
    }

    /**
     * Reads the input stream and calculates the power corresponding to the samples read
     *
     * @param batch (int[]) : batch that contains the different powers
     * @return : number of power samples placed in the batch array
     * @throws IOException              if an input/output error occurs
     * @throws IllegalArgumentException if the length of the batch parameter is not equal to the size of a batch
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batchSize == batch.length);

        int nbOfSamplesRead = samplesDecoder.readBatch(samples);

        int I, Q, j = 0, oldestValue = 0;
        for (int i = 0; i < nbOfSamplesRead; i += 2) {
            circularArray[i % SIZE] = samples[i];
            circularArray[(i + 1) % SIZE] = samples[i + 1];

            I = circularArray[(oldestValue + 1) % SIZE] - circularArray[(oldestValue + 3) % SIZE] +
                    circularArray[(oldestValue + 5) % SIZE] - circularArray[(oldestValue + 7) % SIZE];
            Q = circularArray[oldestValue % SIZE] - circularArray[(oldestValue + 2) % SIZE] +
                    circularArray[(oldestValue + 4) % SIZE] - circularArray[(oldestValue + 6) % SIZE];

            batch[j] = I * I + Q * Q;
            oldestValue = (oldestValue + 2) % SIZE;
            ++j;
        }

        return j;
    }

}
