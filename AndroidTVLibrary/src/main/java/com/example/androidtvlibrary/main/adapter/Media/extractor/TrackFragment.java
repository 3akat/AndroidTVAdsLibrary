package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.Media.ExtractorInput;
import com.example.androidtvlibrary.main.adapter.Media.ParsableByteArray;

import java.io.IOException;

public final class TrackFragment {

    /**
     * The default values for samples from the track fragment header.
     */
    public DefaultSampleValues header;
    /**
     * The position (byte offset) of the start of fragment.
     */
    public long atomPosition;
    /**
     * The position (byte offset) of the start of data contained in the fragment.
     */
    public long dataPosition;
    /**
     * The position (byte offset) of the start of auxiliary data.
     */
    public long auxiliaryDataPosition;
    /**
     * The number of track runs of the fragment.
     */
    public int trunCount;
    /**
     * The total number of samples in the fragment.
     */
    public int sampleCount;
    /**
     * The position (byte offset) of the start of sample data of each track run in the fragment.
     */
    public long[] trunDataPosition;
    /**
     * The number of samples contained by each track run in the fragment.
     */
    public int[] trunLength;
    /**
     * The size of each sample in the fragment.
     */
    public int[] sampleSizeTable;
    /** The composition time offset of each sample in the fragment, in microseconds. */
    public int[] sampleCompositionTimeOffsetUsTable;
    /** The decoding time of each sample in the fragment, in microseconds. */
    public long[] sampleDecodingTimeUsTable;
    /**
     * Indicates which samples are sync frames.
     */
    public boolean[] sampleIsSyncFrameTable;
    /**
     * Whether the fragment defines encryption data.
     */
    public boolean definesEncryptionData;
    /**
     * If {@link #definesEncryptionData} is true, indicates which samples use sub-sample encryption.
     * Undefined otherwise.
     */
    public boolean[] sampleHasSubsampleEncryptionTable;
    /**
     * Fragment specific track encryption. May be null.
     */
    public TrackEncryptionBox trackEncryptionBox;
    /**
     * If {@link #definesEncryptionData} is true, indicates the length of the sample encryption data.
     * Undefined otherwise.
     */
    public int sampleEncryptionDataLength;
    /**
     * If {@link #definesEncryptionData} is true, contains binary sample encryption data. Undefined
     * otherwise.
     */
    public ParsableByteArray sampleEncryptionData;
    /**
     * Whether {@link #sampleEncryptionData} needs populating with the actual encryption data.
     */
    public boolean sampleEncryptionDataNeedsFill;
    /**
     * The absolute decode time of the start of the next fragment.
     */
    public long nextFragmentDecodeTime;

    /**
     * Resets the fragment.
     * <p>
     * {@link #sampleCount} and {@link #nextFragmentDecodeTime} are set to 0, and both
     * {@link #definesEncryptionData} and {@link #sampleEncryptionDataNeedsFill} is set to false,
     * and {@link #trackEncryptionBox} is set to null.
     */
    public void reset() {
        trunCount = 0;
        nextFragmentDecodeTime = 0;
        definesEncryptionData = false;
        sampleEncryptionDataNeedsFill = false;
        trackEncryptionBox = null;
    }

    /**
     * Configures the fragment for the specified number of samples.
     * <p>
     * The {@link #sampleCount} of the fragment is set to the specified sample count, and the
     * contained tables are resized if necessary such that they are at least this length.
     *
     * @param sampleCount The number of samples in the new run.
     */
    public void initTables(int trunCount, int sampleCount) {
        this.trunCount = trunCount;
        this.sampleCount = sampleCount;
        if (trunLength == null || trunLength.length < trunCount) {
            trunDataPosition = new long[trunCount];
            trunLength = new int[trunCount];
        }
        if (sampleSizeTable == null || sampleSizeTable.length < sampleCount) {
            // Size the tables 25% larger than needed, so as to make future resize operations less
            // likely. The choice of 25% is relatively arbitrary.
            int tableSize = (sampleCount * 125) / 100;
            sampleSizeTable = new int[tableSize];
            sampleCompositionTimeOffsetUsTable = new int[tableSize];
            sampleDecodingTimeUsTable = new long[tableSize];
            sampleIsSyncFrameTable = new boolean[tableSize];
            sampleHasSubsampleEncryptionTable = new boolean[tableSize];
        }
    }

    /**
     * Configures the fragment to be one that defines encryption data of the specified length.
     * <p>
     * {@link #definesEncryptionData} is set to true, {@link #sampleEncryptionDataLength} is set to
     * the specified length, and {@link #sampleEncryptionData} is resized if necessary such that it
     * is at least this length.
     *
     * @param length The length in bytes of the encryption data.
     */
    public void initEncryptionData(int length) {
        if (sampleEncryptionData == null || sampleEncryptionData.limit() < length) {
            sampleEncryptionData = new ParsableByteArray(length);
        }
        sampleEncryptionDataLength = length;
        definesEncryptionData = true;
        sampleEncryptionDataNeedsFill = true;
    }

    /**
     * Fills {@link #sampleEncryptionData} from the provided input.
     *
     * @param input An {@link ExtractorInput} from which to read the encryption data.
     */
    public void fillEncryptionData(ExtractorInput input) throws IOException, InterruptedException {
        input.readFully(sampleEncryptionData.data, 0, sampleEncryptionDataLength);
        sampleEncryptionData.setPosition(0);
        sampleEncryptionDataNeedsFill = false;
    }

    /**
     * Fills {@link #sampleEncryptionData} from the provided source.
     *
     * @param source A source from which to read the encryption data.
     */
    public void fillEncryptionData(ParsableByteArray source) {
        source.readBytes(sampleEncryptionData.data, 0, sampleEncryptionDataLength);
        sampleEncryptionData.setPosition(0);
        sampleEncryptionDataNeedsFill = false;
    }

    /**
     * Returns the sample presentation timestamp in microseconds.
     *
     * @param index The sample index.
     * @return The presentation timestamps of this sample in microseconds.
     */
    public long getSamplePresentationTimeUs(int index) {
        return sampleDecodingTimeUsTable[index] + sampleCompositionTimeOffsetUsTable[index];
    }

    /** Returns whether the sample at the given index has a subsample encryption table. */
    public boolean sampleHasSubsampleEncryptionTable(int index) {
        return definesEncryptionData && sampleHasSubsampleEncryptionTable[index];
    }
}
