package com.example.androidtvlibrary.main.adapter;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.wow.PlaybackParameters;

public abstract class TestPlayerBase implements TestPlayer {

    private boolean playWhenReady;

    protected final Timeline.Window window;

    public boolean getPlayWhenReady() {
        return playWhenReady;
    }

    public TestPlayerBase() {
        window = new Timeline.Window();
    }

    @Override
    public final boolean isPlaying() {
        return getPlaybackState() == TestPlayer.STATE_READY
                && getPlayWhenReady()
                && getPlaybackSuppressionReason() == PLAYBACK_SUPPRESSION_REASON_NONE;
    }

    @Override
    public final void seekToDefaultPosition() {
        seekToDefaultPosition(getCurrentWindowIndex());
    }

    @Override
    public final void seekToDefaultPosition(int windowIndex) {
        seekTo(windowIndex, /* positionMs= */ C.TIME_UNSET);
    }

    @Override
    public final void seekTo(long positionMs) {
        seekTo(getCurrentWindowIndex(), positionMs);
    }

    @Override
    public final boolean hasPrevious() {
        return getPreviousWindowIndex() != C.INDEX_UNSET;
    }

    @Override
    public final void previous() {
        int previousWindowIndex = getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET) {
            seekToDefaultPosition(previousWindowIndex);
        }
    }

    @Override
    public final boolean hasNext() {
        return getNextWindowIndex() != C.INDEX_UNSET;
    }

    @Override
    public final void next() {
        int nextWindowIndex = getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            seekToDefaultPosition(nextWindowIndex);
        }
    }

    @Override
    public final void stop() {
        stop(/* reset= */ false);
    }

    @Override
    public final int getNextWindowIndex() {
        Timeline timeline = getCurrentTimeline();
        return timeline.isEmpty()
                ? C.INDEX_UNSET
                : timeline.getNextWindowIndex(
                getCurrentWindowIndex(), getRepeatModeForNavigation(), getShuffleModeEnabled());
    }

    @Override
    public final int getPreviousWindowIndex() {
        Timeline timeline = getCurrentTimeline();
        return timeline.isEmpty()
                ? C.INDEX_UNSET
                : timeline.getPreviousWindowIndex(
                getCurrentWindowIndex(), getRepeatModeForNavigation(), getShuffleModeEnabled());
    }

    @Override
    @Nullable
    public final Object getCurrentTag() {
        Timeline timeline = getCurrentTimeline();
        return timeline.isEmpty() ? null : timeline.getWindow(getCurrentWindowIndex(), window).tag;
    }

    @Override
    @Nullable
    public final Object getCurrentManifest() {
        Timeline timeline = getCurrentTimeline();
        return timeline.isEmpty() ? null : timeline.getWindow(getCurrentWindowIndex(), window).manifest;
    }

    @Override
    public final int getBufferedPercentage() {
        long position = getBufferedPosition();
        long duration = getDuration();
        return position == C.TIME_UNSET || duration == C.TIME_UNSET
                ? 0
                : duration == 0 ? 100 : Util.constrainValue((int) ((position * 100) / duration), 0, 100);
    }

    @Override
    public final boolean isCurrentWindowDynamic() {
        Timeline timeline = getCurrentTimeline();
        return !timeline.isEmpty() && timeline.getWindow(getCurrentWindowIndex(), window).isDynamic;
    }

    @Override
    public final boolean isCurrentWindowLive() {
        Timeline timeline = getCurrentTimeline();
        return !timeline.isEmpty() && timeline.getWindow(getCurrentWindowIndex(), window).isLive;
    }

    @Override
    public final boolean isCurrentWindowSeekable() {
        Timeline timeline = getCurrentTimeline();
        return !timeline.isEmpty() && timeline.getWindow(getCurrentWindowIndex(), window).isSeekable;
    }

    @Override
    public final long getContentDuration() {
        Timeline timeline = getCurrentTimeline();
        return timeline.isEmpty()
                ? C.TIME_UNSET
                : timeline.getWindow(getCurrentWindowIndex(), window).getDurationMs();
    }

    @RepeatMode
    private int getRepeatModeForNavigation() {
        @RepeatMode int repeatMode = getRepeatMode();
        return repeatMode == REPEAT_MODE_ONE ? REPEAT_MODE_OFF : repeatMode;
    }

    /**
     * Holds a listener reference.
     */
    protected static final class ListenerHolder {

        /**
         * The listener on which {link #invoke} will execute {@link ListenerInvocation listener
         * invocations}.
         */
        public final EventListener listener;

        private boolean released;

        public ListenerHolder(EventListener listener) {
            this.listener = listener;
        }

        /**
         * Prevents any further {@link ListenerInvocation} to be executed on {@link #listener}.
         */
        public void release() {
            released = true;
        }

        /**
         * Executes the given {@link ListenerInvocation} on {@link #listener}. Does nothing if {@link
         * #release} has been called on this instance.
         */
        public void invoke(ListenerInvocation listenerInvocation) {
            if (!released) {
                listenerInvocation.invokeListener(listener);
            }
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            return listener.equals(((ListenerHolder) other).listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    protected interface ListenerInvocation {

        /**
         * Executes the invocation on the given {@link EventListener}.
         */
        void invokeListener(EventListener listener);
    }

}