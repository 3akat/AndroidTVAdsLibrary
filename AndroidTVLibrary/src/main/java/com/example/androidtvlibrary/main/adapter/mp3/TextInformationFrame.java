package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Util;

public final class TextInformationFrame extends Id3Frame {

    @Nullable
    public final String description;
    public final String value;

    public TextInformationFrame(String id, @Nullable String description, String value) {
        super(id);
        this.description = description;
        this.value = value;
    }

    /* package */ TextInformationFrame(Parcel in) {
        super(castNonNull(in.readString()));
        description = in.readString();
        value = castNonNull(in.readString());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TextInformationFrame other = (TextInformationFrame) obj;
        return id.equals(other.id) && Util.areEqual(description, other.description)
                && Util.areEqual(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + ": description=" + description + ": value=" + value;
    }

    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(value);
    }

    public static final Parcelable.Creator<TextInformationFrame> CREATOR =
            new Parcelable.Creator<TextInformationFrame>() {

                @Override
                public TextInformationFrame createFromParcel(Parcel in) {
                    return new TextInformationFrame(in);
                }

                @Override
                public TextInformationFrame[] newArray(int size) {
                    return new TextInformationFrame[size];
                }

            };

}
