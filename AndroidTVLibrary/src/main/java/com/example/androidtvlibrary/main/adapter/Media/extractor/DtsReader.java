package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Format;
import com.example.androidtvlibrary.main.adapter.Media.ExtractorOutput;
import com.example.androidtvlibrary.main.adapter.Media.ParsableByteArray;
import com.example.androidtvlibrary.main.adapter.Media.TrackOutput;

public final class DtsReader implements ElementaryStreamReader {

    private static final int STATE_FINDING_SYNC = 0;
    private static final int STATE_READING_HEADER = 1;
    private static final int STATE_READING_SAMPLE = 2;

    private static final int HEADER_SIZE = 18;

    private final ParsableByteArray headerScratchBytes;
    private final String language;

    private String formatId;
    private TrackOutput output;

    private int state;
    private int bytesRead;

    // Used to find the header.
    private int syncBytes;

    // Used when parsing the header.
    private long sampleDurationUs;
    private Format format;
    private int sampleSize;

    // Used when reading the samples.
    private long timeUs;

    /**
     * Constructs a new reader for DTS elementary streams.
     *
     * @param language Track language.
     */
    public DtsReader(String language) {
        headerScratchBytes = new ParsableByteArray(new byte[HEADER_SIZE]);
        state = STATE_FINDING_SYNC;
        this.language = language;
    }

    @Override
    public void seek() {
        state = STATE_FINDING_SYNC;
        bytesRead = 0;
        syncBytes = 0;
    }

    @Override
    public void createTracks(ExtractorOutput extractorOutput, TsPayloadReader.TrackIdGenerator idGenerator) {
        idGenerator.generateNewId();
        formatId = idGenerator.getFormatId();
        output = extractorOutput.track(idGenerator.getTrackId(), C.TRACK_TYPE_AUDIO);
    }

    @Override
    public void packetStarted(long pesTimeUs, @TsPayloadReader.Flags int flags) {
        timeUs = pesTimeUs;
    }

    @Override
    public void consume(ParsableByteArray data) {
        while (data.bytesLeft() > 0) {
            switch (state) {
                case STATE_FINDING_SYNC:
                    if (skipToNextSync(data)) {
                        state = STATE_READING_HEADER;
                    }
                    break;
                case STATE_READING_HEADER:
                    if (continueRead(data, headerScratchBytes.data, HEADER_SIZE)) {
                        parseHeader();
                        headerScratchBytes.setPosition(0);
                        output.sampleData(headerScratchBytes, HEADER_SIZE);
                        state = STATE_READING_SAMPLE;
                    }
                    break;
                case STATE_READING_SAMPLE:
                    int bytesToRead = Math.min(data.bytesLeft(), sampleSize - bytesRead);
                    output.sampleData(data, bytesToRead);
                    bytesRead += bytesToRead;
                    if (bytesRead == sampleSize) {
                        output.sampleMetadata(timeUs, C.BUFFER_FLAG_KEY_FRAME, sampleSize, 0, null);
                        timeUs += sampleDurationUs;
                        state = STATE_FINDING_SYNC;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    @Override
    public void packetFinished() {
        // Do nothing.
    }

    /**
     * Continues a read from the provided {@code source} into a given {@code target}. It's assumed
     * that the data should be written into {@code target} starting from an offset of zero.
     *
     * @param source The source from which to read.
     * @param target The target into which data is to be read.
     * @param targetLength The target length of the read.
     * @return Whether the target length was reached.
     */
    private boolean continueRead(ParsableByteArray source, byte[] target, int targetLength) {
        int bytesToRead = Math.min(source.bytesLeft(), targetLength - bytesRead);
        source.readBytes(target, bytesRead, bytesToRead);
        bytesRead += bytesToRead;
        return bytesRead == targetLength;
    }

    /**
     * Locates the next SYNC value in the buffer, advancing the position to the byte that immediately
     * follows it. If SYNC was not located, the position is advanced to the limit.
     *
     * @param pesBuffer The buffer whose position should be advanced.
     * @return Whether SYNC was found.
     */
    private boolean skipToNextSync(ParsableByteArray pesBuffer) {
        while (pesBuffer.bytesLeft() > 0) {
            syncBytes <<= 8;
            syncBytes |= pesBuffer.readUnsignedByte();
            if (DtsUtil.isSyncWord(syncBytes)) {
                headerScratchBytes.data[0] = (byte) ((syncBytes >> 24) & 0xFF);
                headerScratchBytes.data[1] = (byte) ((syncBytes >> 16) & 0xFF);
                headerScratchBytes.data[2] = (byte) ((syncBytes >> 8) & 0xFF);
                headerScratchBytes.data[3] = (byte) (syncBytes & 0xFF);
                bytesRead = 4;
                syncBytes = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the sample header.
     */
    private void parseHeader() {
        byte[] frameData = headerScratchBytes.data;
        if (format == null) {
            format = DtsUtil.parseDtsFormat(frameData, formatId, language, null);
            output.format(format);
        }
        sampleSize = DtsUtil.getDtsFrameSize(frameData);
        // In this class a sample is an access unit (frame in DTS), but the format's sample rate
        // specifies the number of PCM audio samples per second.
        sampleDurationUs = (int) (C.MICROS_PER_SECOND
                * DtsUtil.parseDtsAudioSampleCount(frameData) / format.sampleRate);
    }

}
