package com.example.androidtvlibrary.main.adapter.player;

import static com.example.androidtvlibrary.main.adapter.Util.castNonNull;

import android.os.Handler;
import android.os.SystemClock;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.Assertions;
import com.example.androidtvlibrary.main.adapter.Format;

public interface VideoRendererEventListener {

    /**
     * Called when the renderer is enabled.
     *
     * @param counters {@link DecoderCounters} that will be updated by the renderer for as long as it
     *     remains enabled.
     */
    default void onVideoEnabled(DecoderCounters counters) {}

    /**
     * Called when a decoder is created.
     *
     * @param decoderName The decoder that was created.
     * @param initializedTimestampMs {@link SystemClock#elapsedRealtime()} when initialization
     *     finished.
     * @param initializationDurationMs The time taken to initialize the decoder in milliseconds.
     */
    default void onVideoDecoderInitialized(
            String decoderName, long initializedTimestampMs, long initializationDurationMs) {}

    /**
     * Called when the format of the media being consumed by the renderer changes.
     *
     * @param format The new format.
     */
    default void onVideoInputFormatChanged(Format format) {}

    /**
     * Called to report the number of frames dropped by the renderer. Dropped frames are reported
     * whenever the renderer is stopped having dropped frames, and optionally, whenever the count
     * reaches a specified threshold whilst the renderer is started.
     *
     * @param count The number of dropped frames.
     * @param elapsedMs The duration in milliseconds over which the frames were dropped. This duration
     *     is timed from when the renderer was started or from when dropped frames were last reported
     *     (whichever was more recent), and not from when the first of the reported drops occurred.
     */
    default void onDroppedFrames(int count, long elapsedMs) {}

    /**
     * Called before a frame is rendered for the first time since setting the surface, and each time
     * there's a change in the size, rotation or pixel aspect ratio of the video being rendered.
     *
     * @param width The video width in pixels.
     * @param height The video height in pixels.
     * @param unappliedRotationDegrees For videos that require a rotation, this is the clockwise
     *     rotation in degrees that the application should apply for the video for it to be rendered
     *     in the correct orientation. This value will always be zero on API levels 21 and above,
     *     since the renderer will apply all necessary rotations internally. On earlier API levels
     *     this is not possible. Applications that use {@link TextureView} can apply the rotation by
     *     calling {@link TextureView#setTransform}. Applications that do not expect to encounter
     *     rotated videos can safely ignore this parameter.
     * @param pixelWidthHeightRatio The width to height ratio of each pixel. For the normal case of
     *     square pixels this will be equal to 1.0. Different values are indicative of anamorphic
     *     content.
     */
    default void onVideoSizeChanged(
            int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {}

    /**
     * Called when a frame is rendered for the first time since setting the surface, and when a frame
     * is rendered for the first time since the renderer was reset.
     *
     * @param surface The {@link Surface} to which a first frame has been rendered, or {@code null} if
     *     the renderer renders to something that isn't a {@link Surface}.
     */
    default void onRenderedFirstFrame(@Nullable Surface surface) {}

    /**
     * Called when the renderer is disabled.
     *
     * @param counters {@link DecoderCounters} that were updated by the renderer.
     */
    default void onVideoDisabled(DecoderCounters counters) {}

    /**
     * Dispatches events to a {@link VideoRendererEventListener}.
     */
    final class EventDispatcher {

        @Nullable private final Handler handler;
        @Nullable private final VideoRendererEventListener listener;

        /**
         * @param handler A handler for dispatching events, or null if creating a dummy instance.
         * @param listener The listener to which events should be dispatched, or null if creating a
         *     dummy instance.
         */
        public EventDispatcher(@Nullable Handler handler,
                               @Nullable VideoRendererEventListener listener) {
            this.handler = listener != null ? Assertions.checkNotNull(handler) : null;
            this.listener = listener;
        }

        /** Invokes {@link VideoRendererEventListener#onVideoEnabled(DecoderCounters)}. */
        public void enabled(DecoderCounters decoderCounters) {
            if (handler != null) {
                handler.post(() -> castNonNull(listener).onVideoEnabled(decoderCounters));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onVideoDecoderInitialized(String, long, long)}. */
        public void decoderInitialized(
                String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            if (handler != null) {
                handler.post(
                        () ->
                                castNonNull(listener)
                                        .onVideoDecoderInitialized(
                                                decoderName, initializedTimestampMs, initializationDurationMs));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onVideoInputFormatChanged(Format)}. */
        public void inputFormatChanged(Format format) {
            if (handler != null) {
                handler.post(() -> castNonNull(listener).onVideoInputFormatChanged(format));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onDroppedFrames(int, long)}. */
        public void droppedFrames(int droppedFrameCount, long elapsedMs) {
            if (handler != null) {
                handler.post(() -> castNonNull(listener).onDroppedFrames(droppedFrameCount, elapsedMs));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onVideoSizeChanged(int, int, int, float)}. */
        public void videoSizeChanged(
                int width,
                int height,
                final int unappliedRotationDegrees,
                final float pixelWidthHeightRatio) {
            if (handler != null) {
                handler.post(
                        () ->
                                castNonNull(listener)
                                        .onVideoSizeChanged(
                                                width, height, unappliedRotationDegrees, pixelWidthHeightRatio));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onRenderedFirstFrame(Surface)}. */
        public void renderedFirstFrame(@Nullable Surface surface) {
            if (handler != null) {
                handler.post(() -> castNonNull(listener).onRenderedFirstFrame(surface));
            }
        }

        /** Invokes {@link VideoRendererEventListener#onVideoDisabled(DecoderCounters)}. */
        public void disabled(DecoderCounters counters) {
            counters.ensureUpdated();
            if (handler != null) {
                handler.post(
                        () -> {
                            counters.ensureUpdated();
                            castNonNull(listener).onVideoDisabled(counters);
                        });
            }
        }

    }

}
