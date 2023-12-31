package com.example.androidtvlibrary.main.adapter.Media.extractor;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Format;
import com.example.androidtvlibrary.main.adapter.Media.ParsableByteArray;
import com.example.androidtvlibrary.main.adapter.MimeTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OpusReader extends StreamReader {

    private static final int DEFAULT_SEEK_PRE_ROLL_SAMPLES = 3840;

    /**
     * Opus streams are always decoded at 48000 Hz.
     */
    private static final int SAMPLE_RATE = 48000;

    private static final int OPUS_CODE = 0x4f707573;
    private static final byte[] OPUS_SIGNATURE = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};

    private boolean headerRead;

    public static boolean verifyBitstreamType(ParsableByteArray data) {
        if (data.bytesLeft() < OPUS_SIGNATURE.length) {
            return false;
        }
        byte[] header = new byte[OPUS_SIGNATURE.length];
        data.readBytes(header, 0, OPUS_SIGNATURE.length);
        return Arrays.equals(header, OPUS_SIGNATURE);
    }

    @Override
    protected void reset(boolean headerData) {
        super.reset(headerData);
        if (headerData) {
            headerRead = false;
        }
    }

    @Override
    protected long preparePayload(ParsableByteArray packet) {
        return convertTimeToGranule(getPacketDurationUs(packet.data));
    }

    @Override
    protected boolean readHeaders(ParsableByteArray packet, long position, SetupData setupData) {
        if (!headerRead) {
            byte[] metadata = Arrays.copyOf(packet.data, packet.limit());
            int channelCount = metadata[9] & 0xFF;
            int preskip = ((metadata[11] & 0xFF) << 8) | (metadata[10] & 0xFF);

            List<byte[]> initializationData = new ArrayList<>(3);
            initializationData.add(metadata);
            putNativeOrderLong(initializationData, preskip);
            putNativeOrderLong(initializationData, DEFAULT_SEEK_PRE_ROLL_SAMPLES);

            setupData.format = Format.createAudioSampleFormat(null, MimeTypes.AUDIO_OPUS, null,
                    Format.NO_VALUE, Format.NO_VALUE, channelCount, SAMPLE_RATE, initializationData, null, 0,
                    null);
            headerRead = true;
        } else {
            boolean headerPacket = packet.readInt() == OPUS_CODE;
            packet.setPosition(0);
            return headerPacket;
        }
        return true;
    }

    private void putNativeOrderLong(List<byte[]> initializationData, int samples) {
        long ns = (samples * C.NANOS_PER_SECOND) / SAMPLE_RATE;
        byte[] array = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(ns).array();
        initializationData.add(array);
    }

    /**
     * Returns the duration of the given audio packet.
     *
     * @param packet Contains audio data.
     * @return Returns the duration of the given audio packet.
     */
    private long getPacketDurationUs(byte[] packet) {
        int toc = packet[0] & 0xFF;
        int frames;
        switch (toc & 0x3) {
            case 0:
                frames = 1;
                break;
            case 1:
            case 2:
                frames = 2;
                break;
            default:
                frames = packet[1] & 0x3F;
                break;
        }

        int config = toc >> 3;
        int length = config & 0x3;
        if (config >= 16) {
            length = 2500 << length;
        } else if (config >= 12) {
            length = 10000 << (length & 0x1);
        } else if (length == 3) {
            length = 60000;
        } else {
            length = 10000 << length;
        }
        return (long) frames * length;
    }
}

