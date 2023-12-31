package com.example.androidtvlibrary.main.adapter;

import androidx.annotation.Nullable;

import java.util.Arrays;

public final class TrackSelectionArray {

    /** The length of this array. */
    public final int length;

    private final  TrackSelection[] trackSelections;

    // Lazily initialized hashcode.
    private int hashCode;

    /** @param trackSelections The selections. Must not be null, but may contain null elements. */
    public TrackSelectionArray( TrackSelection... trackSelections) {
        this.trackSelections = trackSelections;
        this.length = trackSelections.length;
    }

    /**
     * Returns the selection at a given index.
     *
     * @param index The index of the selection.
     * @return The selection.
     */
    @Nullable
    public TrackSelection get(int index) {
        return trackSelections[index];
    }

    /** Returns the selections in a newly allocated array. */
    public  TrackSelection[] getAll() {
        return trackSelections.clone();
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 31 * result + Arrays.hashCode(trackSelections);
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TrackSelectionArray other = (TrackSelectionArray) obj;
        return Arrays.equals(trackSelections, other.trackSelections);
    }

}

