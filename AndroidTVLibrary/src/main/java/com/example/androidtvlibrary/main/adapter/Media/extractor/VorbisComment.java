package com.example.androidtvlibrary.main.adapter.Media.extractor;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Metadata;

public final class VorbisComment implements Metadata.Entry {

    /** The key. */
    public final String key;

    /** The value. */
    public final String value;

    /**
     * @param key The key.
     * @param value The value.
     */
    public VorbisComment(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /* package */ VorbisComment(Parcel in) {
        this.key = castNonNull(in.readString());
        this.value = castNonNull(in.readString());
    }

    @Override
    public String toString() {
        return "VC: " + key + "=" + value;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VorbisComment other = (VorbisComment) obj;
        return key.equals(other.key) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<VorbisComment> CREATOR =
            new Parcelable.Creator<VorbisComment>() {

                @Override
                public VorbisComment createFromParcel(Parcel in) {
                    return new VorbisComment(in);
                }

                @Override
                public VorbisComment[] newArray(int size) {
                    return new VorbisComment[size];
                }
            };
}
