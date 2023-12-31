package com.example.androidtvlibrary.main.adapter.player;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Assertions;
import com.example.androidtvlibrary.main.adapter.C;

import java.nio.ByteBuffer;

public final class ChannelMappingAudioProcessor extends BaseAudioProcessor {

    @Nullable
    private int[] pendingOutputChannels;
    @Nullable private int[] outputChannels;

    /**
     * Resets the channel mapping. After calling this method, call {@link #configure(AudioFormat)} to
     * start using the new channel map.
     *
     * @param outputChannels The mapping from input to output channel indices, or {@code null} to
     *     leave the input unchanged.
     * @see AudioSink#configure(int, int, int, int, int[], int, int)
     */
    public void setChannelMap(@Nullable int[] outputChannels) {
        pendingOutputChannels = outputChannels;
    }

    @Override
    public AudioFormat onConfigure(AudioFormat inputAudioFormat)
            throws UnhandledAudioFormatException {
        @Nullable int[] outputChannels = pendingOutputChannels;
        if (outputChannels == null) {
            return AudioFormat.NOT_SET;
        }

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw new UnhandledAudioFormatException(inputAudioFormat);
        }

        boolean active = inputAudioFormat.channelCount != outputChannels.length;
        for (int i = 0; i < outputChannels.length; i++) {
            int channelIndex = outputChannels[i];
            if (channelIndex >= inputAudioFormat.channelCount) {
                throw new UnhandledAudioFormatException(inputAudioFormat);
            }
            active |= (channelIndex != i);
        }
        return active
                ? new AudioFormat(inputAudioFormat.sampleRate, outputChannels.length, C.ENCODING_PCM_16BIT)
                : AudioFormat.NOT_SET;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int[] outputChannels = Assertions.checkNotNull(this.outputChannels);
        int position = inputBuffer.position();
        int limit = inputBuffer.limit();
        int frameCount = (limit - position) / inputAudioFormat.bytesPerFrame;
        int outputSize = frameCount * outputAudioFormat.bytesPerFrame;
        ByteBuffer buffer = replaceOutputBuffer(outputSize);
        while (position < limit) {
            for (int channelIndex : outputChannels) {
                buffer.putShort(inputBuffer.getShort(position + 2 * channelIndex));
            }
            position += inputAudioFormat.bytesPerFrame;
        }
        inputBuffer.position(limit);
        buffer.flip();
    }

    @Override
    protected void onFlush() {
        outputChannels = pendingOutputChannels;
    }

    @Override
    protected void onReset() {
        outputChannels = null;
        pendingOutputChannels = null;
    }

}
