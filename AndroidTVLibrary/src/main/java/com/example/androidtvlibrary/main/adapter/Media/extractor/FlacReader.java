package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.Assertions;
import com.example.androidtvlibrary.main.adapter.Media.ExtractorInput;
import com.example.androidtvlibrary.main.adapter.Media.ParsableByteArray;
import com.example.androidtvlibrary.main.adapter.Media.SeekMap;
import com.example.androidtvlibrary.main.adapter.Util;

import java.io.IOException;
import java.util.Arrays;

public final class FlacReader extends StreamReader {

    private static final byte AUDIO_PACKET_TYPE = (byte) 0xFF;

    private static final int FRAME_HEADER_SAMPLE_NUMBER_OFFSET = 4;

    private FlacStreamMetadata streamMetadata;
    private FlacOggSeeker flacOggSeeker;

    public static boolean verifyBitstreamType(ParsableByteArray data) {
        return data.bytesLeft() >= 5 && data.readUnsignedByte() == 0x7F && // packet type
                data.readUnsignedInt() == 0x464C4143; // ASCII signature "FLAC"
    }

    @Override
    protected void reset(boolean headerData) {
        super.reset(headerData);
        if (headerData) {
            streamMetadata = null;
            flacOggSeeker = null;
        }
    }

    private static boolean isAudioPacket(byte[] data) {
        return data[0] == AUDIO_PACKET_TYPE;
    }

    @Override
    protected long preparePayload(ParsableByteArray packet) {
        if (!isAudioPacket(packet.data)) {
            return -1;
        }
        return getFlacFrameBlockSize(packet);
    }

    @Override
    protected boolean readHeaders(ParsableByteArray packet, long position, SetupData setupData) {
        byte[] data = packet.data;
        if (streamMetadata == null) {
            streamMetadata = new FlacStreamMetadata(data, 17);
            byte[] metadata = Arrays.copyOfRange(data, 9, packet.limit());
            setupData.format = streamMetadata.getFormat(metadata, /* id3Metadata= */ null);
        } else if ((data[0] & 0x7F) == FlacConstants.METADATA_TYPE_SEEK_TABLE) {
            flacOggSeeker = new FlacOggSeeker();
            FlacStreamMetadata.SeekTable seekTable =
                    FlacMetadataReader.readSeekTableMetadataBlock(packet);
            streamMetadata = streamMetadata.copyWithSeekTable(seekTable);
        } else if (isAudioPacket(data)) {
            if (flacOggSeeker != null) {
                flacOggSeeker.setFirstFrameOffset(position);
                setupData.oggSeeker = flacOggSeeker;
            }
            return false;
        }
        return true;
    }

    private int getFlacFrameBlockSize(ParsableByteArray packet) {
        int blockSizeKey = (packet.data[2] & 0xFF) >> 4;
        if (blockSizeKey == 6 || blockSizeKey == 7) {
            // Skip the sample number.
            packet.skipBytes(FRAME_HEADER_SAMPLE_NUMBER_OFFSET);
            packet.readUtf8EncodedLong();
        }
        int result = FlacFrameReader.readFrameBlockSizeSamplesFromKey(packet, blockSizeKey);
        packet.setPosition(0);
        return result;
    }

    private class FlacOggSeeker implements OggSeeker {

        private long firstFrameOffset;
        private long pendingSeekGranule;

        public FlacOggSeeker() {
            firstFrameOffset = -1;
            pendingSeekGranule = -1;
        }

        public void setFirstFrameOffset(long firstFrameOffset) {
            this.firstFrameOffset = firstFrameOffset;
        }

        @Override
        public long read(ExtractorInput input) throws IOException, InterruptedException {
            if (pendingSeekGranule >= 0) {
                long result = -(pendingSeekGranule + 2);
                pendingSeekGranule = -1;
                return result;
            }
            return -1;
        }

        @Override
        public void startSeek(long targetGranule) {
            Assertions.checkNotNull(streamMetadata.seekTable);
            long[] seekPointGranules = streamMetadata.seekTable.pointSampleNumbers;
            int index = Util.binarySearchFloor(seekPointGranules, targetGranule, true, true);
            pendingSeekGranule = seekPointGranules[index];
        }

        @Override
        public SeekMap createSeekMap() {
            Assertions.checkState(firstFrameOffset != -1);
            return new FlacSeekTableSeekMap(streamMetadata, firstFrameOffset);
        }

    }

}
