package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Transforms bytes coming from the AirSpy into signed 12-bit samples.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class SamplesDecoder {
    private final InputStream stream;
    private final int batchSize;
    private final byte[] buffer;
    private static final int CENTRALIZING_CONSTANT = 2048;

    /**
     * Creates a decoder of samples received
     *
     * @param stream    (InputStream) : input stream containing octets representing samples received from AirSpy
     * @param batchSize (int) : given size of batch
     * @throws IllegalArgumentException if the batch size is not strictly positive
     * @throws NullPointerException     if the stream is null
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        this.buffer = new byte[Short.BYTES * batchSize];
    }

    /**
     * Reads the input stream and decodes the samples received
     *
     * @param batch (short[]) : batch that contains the decoded 12-bit samples
     * @return : number of converted pairs of octets
     * @throws IOException              if an input/output error occurs           .
     * @throws IllegalArgumentException if the length of the batch parameter is not equal to the size of a batch
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        int nbBytesRead = stream.readNBytes(buffer, 0, buffer.length);
        int nbSamplesRead = nbBytesRead / Short.BYTES;

        for (int i = 0; i < nbSamplesRead; ++i) {
            int weakByte = Byte.toUnsignedInt(buffer[Short.BYTES * i]);
            int strongByte = Byte.toUnsignedInt(buffer[(Short.BYTES * i) + 1]);
            batch[i] = (short) ((strongByte << Byte.SIZE | weakByte) - CENTRALIZING_CONSTANT);
        }

        return nbSamplesRead;
    }


}
