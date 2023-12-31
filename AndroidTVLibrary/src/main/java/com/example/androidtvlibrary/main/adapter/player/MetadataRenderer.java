package com.example.androidtvlibrary.main.adapter.player;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Assertions;
import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Format;
import com.example.androidtvlibrary.main.adapter.Metadata;
import com.example.androidtvlibrary.main.adapter.TestPlayer;
import com.example.androidtvlibrary.main.adapter.Util;
import com.example.androidtvlibrary.main.adapter.mp3.MetadataDecoder;
import com.example.androidtvlibrary.main.adapter.mp3.MetadataInputBuffer;
import com.example.androidtvlibrary.main.adapter.wow.FormatHolder;
import com.example.androidtvlibrary.main.adapter.wow.RendererCapabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MetadataRenderer extends BaseRenderer implements Handler.Callback {

    private static final int MSG_INVOKE_RENDERER = 0;
    // TODO: Holding multiple pending metadata objects is temporary mitigation against
    // https://github.com/google/ExoPlayer/issues/1874. It should be removed once this issue has been
    // addressed.
    private static final int MAX_PENDING_METADATA_COUNT = 5;

    private final MetadataDecoderFactory decoderFactory;
    private final TestPlayer.MetadataOutput output;
    @Nullable
    private final Handler outputHandler;
    private final MetadataInputBuffer buffer;
    private final  Metadata[] pendingMetadata;
    private final long[] pendingMetadataTimestamps;

    private int pendingMetadataIndex;
    private int pendingMetadataCount;
    @Nullable private MetadataDecoder decoder;
    private boolean inputStreamEnded;
    private long subsampleOffsetUs;

    /**
     * @param output The output.
     * @param outputLooper The looper associated with the thread on which the output should be called.
     *     If the output makes use of standard Android UI components, then this should normally be the
     *     looper associated with the application's main thread, which can be obtained using {@link
     *     android.app.Activity#getMainLooper()}. Null may be passed if the output should be called
     *     directly on the player's internal rendering thread.
     */
    public MetadataRenderer(TestPlayer.MetadataOutput output, @Nullable Looper outputLooper) {
        this(output, outputLooper, MetadataDecoderFactory.DEFAULT);
    }

    /**
     * @param output The output.
     * @param outputLooper The looper associated with the thread on which the output should be called.
     *     If the output makes use of standard Android UI components, then this should normally be the
     *     looper associated with the application's main thread, which can be obtained using {@link
     *     android.app.Activity#getMainLooper()}. Null may be passed if the output should be called
     *     directly on the player's internal rendering thread.
     * @param decoderFactory A factory from which to obtain {@link MetadataDecoder} instances.
     */
    public MetadataRenderer(
            TestPlayer.MetadataOutput output, @Nullable Looper outputLooper, MetadataDecoderFactory decoderFactory) {
        super(C.TRACK_TYPE_METADATA);
        this.output = Assertions.checkNotNull(output);
        this.outputHandler =
                outputLooper == null ? null : Util.createHandler(outputLooper, /* callback= */ this);
        this.decoderFactory = Assertions.checkNotNull(decoderFactory);
        buffer = new MetadataInputBuffer();
        pendingMetadata = new Metadata[MAX_PENDING_METADATA_COUNT];
        pendingMetadataTimestamps = new long[MAX_PENDING_METADATA_COUNT];
    }

    @Override
    @Capabilities
    public int supportsFormat(Format format) {
        if (decoderFactory.supportsFormat(format)) {
            return RendererCapabilities.create(
                    supportsFormatDrm(null, format.drmInitData) ? FORMAT_HANDLED : FORMAT_UNSUPPORTED_DRM);
        } else {
            return RendererCapabilities.create(FORMAT_UNSUPPORTED_TYPE);
        }
    }

    @Override
    protected void onStreamChanged(Format[] formats, long offsetUs) {
        decoder = decoderFactory.createDecoder(formats[0]);
    }

    @Override
    protected void onPositionReset(long positionUs, boolean joining) {
        flushPendingMetadata();
        inputStreamEnded = false;
    }

    @Override
    public void render(long positionUs, long elapsedRealtimeUs) {
        if (!inputStreamEnded && pendingMetadataCount < MAX_PENDING_METADATA_COUNT) {
            buffer.clear();
            FormatHolder formatHolder = getFormatHolder();
            int result = readSource(formatHolder, buffer, false);
            if (result == C.RESULT_BUFFER_READ) {
                if (buffer.isEndOfStream()) {
                    inputStreamEnded = true;
                } else if (buffer.isDecodeOnly()) {
                    // Do nothing. Note this assumes that all metadata buffers can be decoded independently.
                    // If we ever need to support a metadata format where this is not the case, we'll need to
                    // pass the buffer to the decoder and discard the output.
                } else {
                    buffer.subsampleOffsetUs = subsampleOffsetUs;
                    buffer.flip();
                    @Nullable Metadata metadata = castNonNull(decoder).decode(buffer);
                    if (metadata != null) {
                        List<Metadata.Entry> entries = new ArrayList<>(metadata.length());
                        decodeWrappedMetadata(metadata, entries);
                        if (!entries.isEmpty()) {
                            Metadata expandedMetadata = new Metadata(entries);
                            int index =
                                    (pendingMetadataIndex + pendingMetadataCount) % MAX_PENDING_METADATA_COUNT;
                            pendingMetadata[index] = expandedMetadata;
                            pendingMetadataTimestamps[index] = buffer.timeUs;
                            pendingMetadataCount++;
                        }
                    }
                }
            } else if (result == C.RESULT_FORMAT_READ) {
                subsampleOffsetUs = Assertions.checkNotNull(formatHolder.format).subsampleOffsetUs;
            }
        }

        if (pendingMetadataCount > 0 && pendingMetadataTimestamps[pendingMetadataIndex] <= positionUs) {
            Metadata metadata = castNonNull(pendingMetadata[pendingMetadataIndex]);
            invokeRenderer(metadata);
            pendingMetadata[pendingMetadataIndex] = null;
            pendingMetadataIndex = (pendingMetadataIndex + 1) % MAX_PENDING_METADATA_COUNT;
            pendingMetadataCount--;
        }
    }

    /**
     * Iterates through {@code metadata.entries} and checks each one to see if contains wrapped
     * metadata. If it does, then we recursively decode the wrapped metadata. If it doesn't (recursion
     * base-case), we add the {@link Metadata.Entry} to {@code decodedEntries} (output parameter).
     */
    private void decodeWrappedMetadata(Metadata metadata, List<Metadata.Entry> decodedEntries) {
        for (int i = 0; i < metadata.length(); i++) {
            @Nullable Format wrappedMetadataFormat = metadata.get(i).getWrappedMetadataFormat();
            if (wrappedMetadataFormat != null && decoderFactory.supportsFormat(wrappedMetadataFormat)) {
                MetadataDecoder wrappedMetadataDecoder =
                        decoderFactory.createDecoder(wrappedMetadataFormat);
                // wrappedMetadataFormat != null so wrappedMetadataBytes must be non-null too.
                byte[] wrappedMetadataBytes =
                        Assertions.checkNotNull(metadata.get(i).getWrappedMetadataBytes());
                buffer.clear();
                buffer.ensureSpaceForWrite(wrappedMetadataBytes.length);
                castNonNull(buffer.data).put(wrappedMetadataBytes);
                buffer.flip();
                @Nullable Metadata innerMetadata = wrappedMetadataDecoder.decode(buffer);
                if (innerMetadata != null) {
                    // The decoding succeeded, so we'll try another level of unwrapping.
                    decodeWrappedMetadata(innerMetadata, decodedEntries);
                }
            } else {
                // Entry doesn't contain any wrapped metadata, so output it directly.
                decodedEntries.add(metadata.get(i));
            }
        }
    }

    @Override
    protected void onDisabled() {
        flushPendingMetadata();
        decoder = null;
    }

    @Override
    public boolean isEnded() {
        return inputStreamEnded;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private void invokeRenderer(Metadata metadata) {
        if (outputHandler != null) {
            outputHandler.obtainMessage(MSG_INVOKE_RENDERER, metadata).sendToTarget();
        } else {
            invokeRendererInternal(metadata);
        }
    }

    private void flushPendingMetadata() {
        Arrays.fill(pendingMetadata, null);
        pendingMetadataIndex = 0;
        pendingMetadataCount = 0;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INVOKE_RENDERER:
                invokeRendererInternal((Metadata) msg.obj);
                return true;
            default:
                // Should never happen.
                throw new IllegalStateException();
        }
    }

    private void invokeRendererInternal(Metadata metadata) {
        output.onMetadata(metadata);
    }

}