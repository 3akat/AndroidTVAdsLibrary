package com.example.androidtvlibrary.main.adapter.wow;

import android.media.MediaCodec;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Media.CryptoInfo;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

public class DecoderInputBuffer extends Buffer {

    /**
     * The buffer replacement mode, which may disable replacement. One of {@link
     * #BUFFER_REPLACEMENT_MODE_DISABLED}, {@link #BUFFER_REPLACEMENT_MODE_NORMAL} or {@link
     * #BUFFER_REPLACEMENT_MODE_DIRECT}.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            BUFFER_REPLACEMENT_MODE_DISABLED,
            BUFFER_REPLACEMENT_MODE_NORMAL,
            BUFFER_REPLACEMENT_MODE_DIRECT
    })
    public @interface BufferReplacementMode {}
    /**
     * Disallows buffer replacement.
     */
    public static final int BUFFER_REPLACEMENT_MODE_DISABLED = 0;
    /**
     * Allows buffer replacement using {@link ByteBuffer#allocate(int)}.
     */
    public static final int BUFFER_REPLACEMENT_MODE_NORMAL = 1;
    /**
     * Allows buffer replacement using {@link ByteBuffer#allocateDirect(int)}.
     */
    public static final int BUFFER_REPLACEMENT_MODE_DIRECT = 2;


    public final CryptoInfo cryptoInfo;

    /** The buffer's data, or {@code null} if no data has been set. */
    @Nullable
    public ByteBuffer data;

    // TODO: Remove this temporary signaling once end-of-stream propagation for clips using content
    // protection is fixed. See [Internal: b/153326944] for details.
    /**
     * Whether the last attempt to read a sample into this buffer failed due to not yet having the DRM
     * keys associated with the next sample.
     */
    public boolean waitingForKeys;

    /**
     * The time at which the sample should be presented.
     */
    public long timeUs;


    @Nullable public ByteBuffer supplementalData;

    @BufferReplacementMode private final int bufferReplacementMode;

    /**
     * Creates a new instance for which {@link #isFlagsOnly()} will return true.
     *
     * @return A new flags only input buffer.
     */
    public static DecoderInputBuffer newFlagsOnlyInstance() {
        return new DecoderInputBuffer(BUFFER_REPLACEMENT_MODE_DISABLED);
    }

    /**
     * @param bufferReplacementMode Determines the behavior of {@link #ensureSpaceForWrite(int)}. One
     *     of {@link #BUFFER_REPLACEMENT_MODE_DISABLED}, {@link #BUFFER_REPLACEMENT_MODE_NORMAL} and
     *     {@link #BUFFER_REPLACEMENT_MODE_DIRECT}.
     */
    public DecoderInputBuffer(@BufferReplacementMode int bufferReplacementMode) {
        this.cryptoInfo = new CryptoInfo();
        this.bufferReplacementMode = bufferReplacementMode;
    }

    /**
     * Clears {@link #supplementalData} and ensures that it's large enough to accommodate {@code
     * length} bytes.
     *
     * @param length The length of the supplemental data that must be accommodated, in bytes.
     */
    public void resetSupplementalData(int length) {
        if (supplementalData == null || supplementalData.capacity() < length) {
            supplementalData = ByteBuffer.allocate(length);
        } else {
            supplementalData.clear();
        }
    }

    /**
     * Ensures that {@link #data} is large enough to accommodate a write of a given length at its
     * current position.
     *
     * <p>If the capacity of {@link #data} is sufficient this method does nothing. If the capacity is
     * insufficient then an attempt is made to replace {@link #data} with a new {@link ByteBuffer}
     * whose capacity is sufficient. Data up to the current position is copied to the new buffer.
     *
     * @param length The length of the write that must be accommodated, in bytes.
     * @throws IllegalStateException If there is insufficient capacity to accommodate the write and
     *     the buffer replacement mode of the holder is {@link #BUFFER_REPLACEMENT_MODE_DISABLED}.
     */
    public void ensureSpaceForWrite(int length) {
        if (data == null) {
            data = createReplacementByteBuffer(length);
            return;
        }
        // Check whether the current buffer is sufficient.
        int capacity = data.capacity();
        int position = data.position();
        int requiredCapacity = position + length;
        if (capacity >= requiredCapacity) {
            return;
        }
        // Instantiate a new buffer if possible.
        ByteBuffer newData = createReplacementByteBuffer(requiredCapacity);
        newData.order(data.order());
        // Copy data up to the current position from the old buffer to the new one.
        if (position > 0) {
            data.flip();
            newData.put(data);
        }
        // Set the new buffer.
        data = newData;
    }

    /**
     * Returns whether the buffer is only able to hold flags, meaning {@link #data} is null and
     * its replacement mode is {@link #BUFFER_REPLACEMENT_MODE_DISABLED}.
     */
    public final boolean isFlagsOnly() {
        return data == null && bufferReplacementMode == BUFFER_REPLACEMENT_MODE_DISABLED;
    }

    /**
     * Returns whether the {@link C#BUFFER_FLAG_ENCRYPTED} flag is set.
     */
    public final boolean isEncrypted() {
        return getFlag(C.BUFFER_FLAG_ENCRYPTED);
    }

    /**
     * Flips {@link #data} and {@link #supplementalData} in preparation for being queued to a decoder.
     *
     * @see java.nio.Buffer#flip()
     */
    public final void flip() {
        data.flip();
        if (supplementalData != null) {
            supplementalData.flip();
        }
    }

    @Override
    public void clear() {
        super.clear();
        if (data != null) {
            data.clear();
        }
        if (supplementalData != null) {
            supplementalData.clear();
        }
        waitingForKeys = false;
    }

    private ByteBuffer createReplacementByteBuffer(int requiredCapacity) {
        if (bufferReplacementMode == BUFFER_REPLACEMENT_MODE_NORMAL) {
            return ByteBuffer.allocate(requiredCapacity);
        } else if (bufferReplacementMode == BUFFER_REPLACEMENT_MODE_DIRECT) {
            return ByteBuffer.allocateDirect(requiredCapacity);
        } else {
            int currentCapacity = data == null ? 0 : data.capacity();
            throw new IllegalStateException("Buffer too small (" + currentCapacity + " < "
                    + requiredCapacity + ")");
        }
    }

}
