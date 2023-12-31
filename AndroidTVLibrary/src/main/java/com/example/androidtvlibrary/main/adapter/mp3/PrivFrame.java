package com.example.androidtvlibrary.main.adapter.mp3;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Util;

import java.util.Arrays;

public final class PrivFrame extends Id3Frame {

    public static final String ID = "PRIV";

    public final String owner;
    public final byte[] privateData;

    public PrivFrame(String owner, byte[] privateData) {
        super(ID);
        this.owner = owner;
        this.privateData = privateData;
    }

    /* package */ PrivFrame(Parcel in) {
        super(ID);
        owner = castNonNull(in.readString());
        privateData = castNonNull(in.createByteArray());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrivFrame other = (PrivFrame) obj;
        return Util.areEqual(owner, other.owner) && Arrays.equals(privateData, other.privateData);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(privateData);
        return result;
    }

    @Override
    public String toString() {
        return id + ": owner=" + owner;
    }
    // Parcelable implementation.

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(owner);
        dest.writeByteArray(privateData);
    }

    public static final Parcelable.Creator<PrivFrame> CREATOR = new Parcelable.Creator<PrivFrame>() {

        @Override
        public PrivFrame createFromParcel(Parcel in) {
            return new PrivFrame(in);
        }

        @Override
        public PrivFrame[] newArray(int size) {
            return new PrivFrame[size];
        }

    };

}
