package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Util;

public final class UrlLinkFrame extends Id3Frame {

    @Nullable
    public final String description;
    public final String url;

    public UrlLinkFrame(String id, @Nullable String description, String url) {
        super(id);
        this.description = description;
        this.url = url;
    }

    /* package */ UrlLinkFrame(Parcel in) {
        super(castNonNull(in.readString()));
        description = in.readString();
        url = castNonNull(in.readString());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UrlLinkFrame other = (UrlLinkFrame) obj;
        return id.equals(other.id) && Util.areEqual(description, other.description)
                && Util.areEqual(url, other.url);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + ": url=" + url;
    }

    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<UrlLinkFrame> CREATOR =
            new Parcelable.Creator<UrlLinkFrame>() {

                @Override
                public UrlLinkFrame createFromParcel(Parcel in) {
                    return new UrlLinkFrame(in);
                }

                @Override
                public UrlLinkFrame[] newArray(int size) {
                    return new UrlLinkFrame[size];
                }

            };

}
