package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Util;

import java.util.Arrays;

public final class ChapterFrame extends Id3Frame {

    public static final String ID = "CHAP";

    public final String chapterId;
    public final int startTimeMs;
    public final int endTimeMs;
    /**
     * The byte offset of the start of the chapter, or {@link C#POSITION_UNSET} if not set.
     */
    public final long startOffset;
    /**
     * The byte offset of the end of the chapter, or {@link C#POSITION_UNSET} if not set.
     */
    public final long endOffset;
    private final Id3Frame[] subFrames;

    public ChapterFrame(String chapterId, int startTimeMs, int endTimeMs, long startOffset,
                        long endOffset, Id3Frame[] subFrames) {
        super(ID);
        this.chapterId = chapterId;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.subFrames = subFrames;
    }

    /* package */ ChapterFrame(Parcel in) {
        super(ID);
        this.chapterId = castNonNull(in.readString());
        this.startTimeMs = in.readInt();
        this.endTimeMs = in.readInt();
        this.startOffset = in.readLong();
        this.endOffset = in.readLong();
        int subFrameCount = in.readInt();
        subFrames = new Id3Frame[subFrameCount];
        for (int i = 0; i < subFrameCount; i++) {
            subFrames[i] = in.readParcelable(Id3Frame.class.getClassLoader());
        }
    }

    /**
     * Returns the number of sub-frames.
     */
    public int getSubFrameCount() {
        return subFrames.length;
    }

    /**
     * Returns the sub-frame at {@code index}.
     */
    public Id3Frame getSubFrame(int index) {
        return subFrames[index];
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChapterFrame other = (ChapterFrame) obj;
        return startTimeMs == other.startTimeMs
                && endTimeMs == other.endTimeMs
                && startOffset == other.startOffset
                && endOffset == other.endOffset
                && Util.areEqual(chapterId, other.chapterId)
                && Arrays.equals(subFrames, other.subFrames);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + startTimeMs;
        result = 31 * result + endTimeMs;
        result = 31 * result + (int) startOffset;
        result = 31 * result + (int) endOffset;
        result = 31 * result + (chapterId != null ? chapterId.hashCode() : 0);
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chapterId);
        dest.writeInt(startTimeMs);
        dest.writeInt(endTimeMs);
        dest.writeLong(startOffset);
        dest.writeLong(endOffset);
        dest.writeInt(subFrames.length);
        for (Id3Frame subFrame : subFrames) {
            dest.writeParcelable(subFrame, 0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChapterFrame> CREATOR = new Creator<ChapterFrame>() {

        @Override
        public ChapterFrame createFromParcel(Parcel in) {
            return new ChapterFrame(in);
        }

        @Override
        public ChapterFrame[] newArray(int size) {
            return new ChapterFrame[size];
        }

    };

}