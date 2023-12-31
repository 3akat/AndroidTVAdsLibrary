package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.Assertions;

public final class VorbisBitArray {

    private final byte[] data;
    private final int byteLimit;

    private int byteOffset;
    private int bitOffset;

    /**
     * Creates a new instance that wraps an existing array.
     *
     * @param data the array to wrap.
     */
    public VorbisBitArray(byte[] data) {
        this.data = data;
        byteLimit = data.length;
    }

    /**
     * Resets the reading position to zero.
     */
    public void reset() {
        byteOffset = 0;
        bitOffset = 0;
    }

    /**
     * Reads a single bit.
     *
     * @return {@code true} if the bit is set, {@code false} otherwise.
     */
    public boolean readBit() {
        boolean returnValue = (((data[byteOffset] & 0xFF) >> bitOffset) & 0x01) == 1;
        skipBits(1);
        return returnValue;
    }

    /**
     * Reads up to 32 bits.
     *
     * @param numBits The number of bits to read.
     * @return An integer whose bottom {@code numBits} bits hold the read data.
     */
    public int readBits(int numBits) {
        int tempByteOffset = byteOffset;
        int bitsRead = Math.min(numBits, 8 - bitOffset);
        int returnValue = ((data[tempByteOffset++] & 0xFF) >> bitOffset) & (0xFF >> (8 - bitsRead));
        while (bitsRead < numBits) {
            returnValue |= (data[tempByteOffset++] & 0xFF) << bitsRead;
            bitsRead += 8;
        }
        returnValue &= 0xFFFFFFFF >>> (32 - numBits);
        skipBits(numBits);
        return returnValue;
    }

    /**
     * Skips {@code numberOfBits} bits.
     *
     * @param numBits The number of bits to skip.
     */
    public void skipBits(int numBits) {
        int numBytes = numBits / 8;
        byteOffset += numBytes;
        bitOffset += numBits - (numBytes * 8);
        if (bitOffset > 7) {
            byteOffset++;
            bitOffset -= 8;
        }
        assertValidOffset();
    }

    /**
     * Returns the reading position in bits.
     */
    public int getPosition() {
        return byteOffset * 8 + bitOffset;
    }

    /**
     * Sets the reading position in bits.
     *
     * @param position The new reading position in bits.
     */
    public void setPosition(int position) {
        byteOffset = position / 8;
        bitOffset = position - (byteOffset * 8);
        assertValidOffset();
    }

    /**
     * Returns the number of remaining bits.
     */
    public int bitsLeft() {
        return (byteLimit - byteOffset) * 8 - bitOffset;
    }

    private void assertValidOffset() {
        // It is fine for position to be at the end of the array, but no further.
        Assertions.checkState(byteOffset >= 0
                && (byteOffset < byteLimit || (byteOffset == byteLimit && bitOffset == 0)));
    }

}
