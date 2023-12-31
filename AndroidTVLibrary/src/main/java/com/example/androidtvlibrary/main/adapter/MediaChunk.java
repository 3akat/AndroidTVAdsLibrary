package com.example.androidtvlibrary.main.adapter;

import androidx.annotation.Nullable;

public abstract class MediaChunk extends Chunk {

    /** The chunk index, or {@link C#INDEX_UNSET} if it is not known. */
    public final long chunkIndex;

    /**
     * @param dataSource The source from which the data should be loaded.
     * @param dataSpec Defines the data to be loaded.
     * @param trackFormat See {@link #trackFormat}.
     * @param trackSelectionReason See {@link #trackSelectionReason}.
     * @param trackSelectionData See {@link #trackSelectionData}.
     * @param startTimeUs The start time of the media contained by the chunk, in microseconds.
     * @param endTimeUs The end time of the media contained by the chunk, in microseconds.
     * @param chunkIndex The index of the chunk, or {@link C#INDEX_UNSET} if it is not known.
     */
    public MediaChunk(
            DataSource dataSource,
            DataSpec dataSpec,
            Format trackFormat,
            int trackSelectionReason,
            @Nullable Object trackSelectionData,
            long startTimeUs,
            long endTimeUs,
            long chunkIndex) {
        super(dataSource, dataSpec, C.DATA_TYPE_MEDIA, trackFormat, trackSelectionReason,
                trackSelectionData, startTimeUs, endTimeUs);
        Assertions.checkNotNull(trackFormat);
        this.chunkIndex = chunkIndex;
    }

    /** Returns the next chunk index or {@link C#INDEX_UNSET} if it is not known. */
    public long getNextChunkIndex() {
        return chunkIndex != C.INDEX_UNSET ? chunkIndex + 1 : C.INDEX_UNSET;
    }

    /**
     * Returns whether the chunk has been fully loaded.
     */
    public abstract boolean isLoadCompleted();

}
