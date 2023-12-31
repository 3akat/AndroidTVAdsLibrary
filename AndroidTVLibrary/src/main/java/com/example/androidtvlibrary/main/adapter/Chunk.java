package com.example.androidtvlibrary.main.adapter;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.util.Assertions;

import java.util.List;
import java.util.Map;

public abstract class Chunk implements Loadable {

    /**
     * The {@link DataSpec} that defines the data to be loaded.
     */
    public final DataSpec dataSpec;
    /**
     * The type of the chunk. One of the {@code DATA_TYPE_*} constants defined in {@link C}. For
     * reporting only.
     */
    public final int type;
    /**
     * The format of the track to which this chunk belongs, or null if the chunk does not belong to
     * a track.
     */
    public final Format trackFormat;
    /**
     * One of the {@link C} {@code SELECTION_REASON_*} constants if the chunk belongs to a track.
     * {@link C#SELECTION_REASON_UNKNOWN} if the chunk does not belong to a track.
     */
    public final int trackSelectionReason;
    /**
     * Optional data associated with the selection of the track to which this chunk belongs. Null if
     * the chunk does not belong to a track.
     */
    @Nullable
    public final Object trackSelectionData;
    /**
     * The start time of the media contained by the chunk, or {@link C#TIME_UNSET} if the data
     * being loaded does not contain media samples.
     */
    public final long startTimeUs;
    /**
     * The end time of the media contained by the chunk, or {@link C#TIME_UNSET} if the data being
     * loaded does not contain media samples.
     */
    public final long endTimeUs;

    protected final StatsDataSource dataSource;

    /**
     * @param dataSource The source from which the data should be loaded.
     * @param dataSpec Defines the data to be loaded.
     * @param type See {@link #type}.
     * @param trackFormat See {@link #trackFormat}.
     * @param trackSelectionReason See {@link #trackSelectionReason}.
     * @param trackSelectionData See {@link #trackSelectionData}.
     * @param startTimeUs See {@link #startTimeUs}.
     * @param endTimeUs See {@link #endTimeUs}.
     */
    public Chunk(
            DataSource dataSource,
            DataSpec dataSpec,
            int type,
            Format trackFormat,
            int trackSelectionReason,
            @Nullable Object trackSelectionData,
            long startTimeUs,
            long endTimeUs) {
        this.dataSource = new StatsDataSource(dataSource);
        this.dataSpec = dataSpec;
        this.type = type;
        this.trackFormat = trackFormat;
        this.trackSelectionReason = trackSelectionReason;
        this.trackSelectionData = trackSelectionData;
        this.startTimeUs = startTimeUs;
        this.endTimeUs = endTimeUs;
    }

    /**
     * Returns the duration of the chunk in microseconds.
     */
    public final long getDurationUs() {
        return endTimeUs - startTimeUs;
    }

    /**
     * Returns the number of bytes that have been loaded. Must only be called after the load
     * completed, failed, or was canceled.
     */
    public final long bytesLoaded() {
        return dataSource.getBytesRead();
    }

    /**
     * Returns the {@link Uri} associated with the last {@link DataSource#open} call. If redirection
     * occurred, this is the redirected uri. Must only be called after the load completed, failed, or
     * was canceled.
     *
     * @see DataSource#getUri()
     */
    public final Uri getUri() {
        return dataSource.getLastOpenedUri();
    }

    /**
     * Returns the response headers associated with the last {@link DataSource#open} call. Must only
     * be called after the load completed, failed, or was canceled.
     *
     * @see DataSource#getResponseHeaders()
     */
    public final Map<String, List<String>> getResponseHeaders() {
        return dataSource.getLastResponseHeaders();
    }
}
