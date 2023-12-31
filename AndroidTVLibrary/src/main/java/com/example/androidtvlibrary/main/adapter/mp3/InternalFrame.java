package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Util;

public final class InternalFrame extends Id3Frame {

    public static final String ID = "----";

    public final String domain;
    public final String description;
    public final String text;

    public InternalFrame(String domain, String description, String text) {
        super(ID);
        this.domain = domain;
        this.description = description;
        this.text = text;
    }

    /* package */ InternalFrame(Parcel in) {
        super(ID);
        domain = castNonNull(in.readString());
        description = castNonNull(in.readString());
        text = castNonNull(in.readString());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InternalFrame other = (InternalFrame) obj;
        return Util.areEqual(description, other.description)
                && Util.areEqual(domain, other.domain)
                && Util.areEqual(text, other.text);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + ": domain=" + domain + ", description=" + description;
    }

    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(domain);
        dest.writeString(text);
    }

    public static final Creator<InternalFrame> CREATOR =
            new Creator<InternalFrame>() {

                @Override
                public InternalFrame createFromParcel(Parcel in) {
                    return new InternalFrame(in);
                }

                @Override
                public InternalFrame[] newArray(int size) {
                    return new InternalFrame[size];
                }
            };
}
