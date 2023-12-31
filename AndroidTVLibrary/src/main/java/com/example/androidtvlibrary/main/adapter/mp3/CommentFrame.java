package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Util;

public final class CommentFrame extends Id3Frame {

    public static final String ID = "COMM";

    public final String language;
    public final String description;
    public final String text;

    public CommentFrame(String language, String description, String text) {
        super(ID);
        this.language = language;
        this.description = description;
        this.text = text;
    }

    /* package */ CommentFrame(Parcel in) {
        super(ID);
        language = castNonNull(in.readString());
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
        CommentFrame other = (CommentFrame) obj;
        return Util.areEqual(description, other.description) && Util.areEqual(language, other.language)
                && Util.areEqual(text, other.text);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + ": language=" + language + ", description=" + description;
    }

    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(language);
        dest.writeString(text);
    }

    public static final Parcelable.Creator<CommentFrame> CREATOR =
            new Parcelable.Creator<CommentFrame>() {

                @Override
                public CommentFrame createFromParcel(Parcel in) {
                    return new CommentFrame(in);
                }

                @Override
                public CommentFrame[] newArray(int size) {
                    return new CommentFrame[size];
                }

            };

}
