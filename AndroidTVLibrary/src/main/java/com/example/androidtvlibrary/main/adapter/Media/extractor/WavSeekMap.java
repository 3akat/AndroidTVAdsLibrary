package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Media.SeekMap;
import com.example.androidtvlibrary.main.adapter.Media.SeekPoint;
import com.example.androidtvlibrary.main.adapter.Util;

public final class WavSeekMap implements SeekMap {

    private final WavHeader wavHeader;
    private final int framesPerBlock;
    private final long firstBlockPosition;
    private final long blockCount;
    private final long durationUs;

    public WavSeekMap(
            WavHeader wavHeader, int framesPerBlock, long dataStartPosition, long dataEndPosition) {
        this.wavHeader = wavHeader;
        this.framesPerBlock = framesPerBlock;
        this.firstBlockPosition = dataStartPosition;
        this.blockCount = (dataEndPosition - dataStartPosition) / wavHeader.blockSize;
        durationUs = blockIndexToTimeUs(blockCount);
    }

    @Override
    public boolean isSeekable() {
        return true;
    }

    @Override
    public long getDurationUs() {
        return durationUs;
    }

    @Override
    public SeekPoints getSeekPoints(long timeUs) {
        // Calculate the containing block index, constraining to valid indices.
        long blockIndex = (timeUs * wavHeader.frameRateHz) / (C.MICROS_PER_SECOND * framesPerBlock);
        blockIndex = Util.constrainValue(blockIndex, 0, blockCount - 1);

        long seekPosition = firstBlockPosition + (blockIndex * wavHeader.blockSize);
        long seekTimeUs = blockIndexToTimeUs(blockIndex);
        SeekPoint seekPoint = new SeekPoint(seekTimeUs, seekPosition);
        if (seekTimeUs >= timeUs || blockIndex == blockCount - 1) {
            return new SeekPoints(seekPoint);
        } else {
            long secondBlockIndex = blockIndex + 1;
            long secondSeekPosition = firstBlockPosition + (secondBlockIndex * wavHeader.blockSize);
            long secondSeekTimeUs = blockIndexToTimeUs(secondBlockIndex);
            SeekPoint secondSeekPoint = new SeekPoint(secondSeekTimeUs, secondSeekPosition);
            return new SeekPoints(seekPoint, secondSeekPoint);
        }
    }

    private long blockIndexToTimeUs(long blockIndex) {
        return Util.scaleLargeTimestamp(
                blockIndex * framesPerBlock, C.MICROS_PER_SECOND, wavHeader.frameRateHz);
    }
}
