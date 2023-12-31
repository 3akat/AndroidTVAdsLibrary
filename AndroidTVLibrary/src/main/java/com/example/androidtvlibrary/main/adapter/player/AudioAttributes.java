package com.example.androidtvlibrary.main.adapter.player;

import android.annotation.TargetApi;

import androidx.annotation.Nullable;

import com.example.androidtvlibrary.main.adapter.C;
import com.example.androidtvlibrary.main.adapter.Util;

public final class AudioAttributes {

    public static final AudioAttributes DEFAULT = new Builder().build();

    /**
     * Builder for {@link AudioAttributes}.
     */
    public static final class Builder {

        private @C.AudioContentType int contentType;
        private @C.AudioFlags int flags;
        private @C.AudioUsage int usage;
        private @C.AudioAllowedCapturePolicy int allowedCapturePolicy;

        /**
         * Creates a new builder for {@link AudioAttributes}.
         *
         * <p>By default the content type is {@link C#CONTENT_TYPE_UNKNOWN}, usage is {@link
         * C#USAGE_MEDIA}, capture policy is {@link C#ALLOW_CAPTURE_BY_ALL} and no flags are set.
         */
        public Builder() {
            contentType = C.CONTENT_TYPE_UNKNOWN;
            flags = 0;
            usage = C.USAGE_MEDIA;
            allowedCapturePolicy = C.ALLOW_CAPTURE_BY_ALL;
        }

        /**
         * @see android.media.AudioAttributes.Builder#setContentType(int)
         */
        public Builder setContentType(@C.AudioContentType int contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * @see android.media.AudioAttributes.Builder#setFlags(int)
         */
        public Builder setFlags(@C.AudioFlags int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * @see android.media.AudioAttributes.Builder#setUsage(int)
         */
        public Builder setUsage(@C.AudioUsage int usage) {
            this.usage = usage;
            return this;
        }

        /** See {@link android.media.AudioAttributes.Builder#setAllowedCapturePolicy(int)}. */
        public Builder setAllowedCapturePolicy(@C.AudioAllowedCapturePolicy int allowedCapturePolicy) {
            this.allowedCapturePolicy = allowedCapturePolicy;
            return this;
        }

        /** Creates an {@link AudioAttributes} instance from this builder. */
        public AudioAttributes build() {
            return new AudioAttributes(contentType, flags, usage, allowedCapturePolicy);
        }

    }

    public final @C.AudioContentType int contentType;
    public final @C.AudioFlags int flags;
    public final @C.AudioUsage int usage;
    public final @C.AudioAllowedCapturePolicy int allowedCapturePolicy;

    @Nullable
    private android.media.AudioAttributes audioAttributesV21;

    private AudioAttributes(
            @C.AudioContentType int contentType,
            @C.AudioFlags int flags,
            @C.AudioUsage int usage,
            @C.AudioAllowedCapturePolicy int allowedCapturePolicy) {
        this.contentType = contentType;
        this.flags = flags;
        this.usage = usage;
        this.allowedCapturePolicy = allowedCapturePolicy;
    }

    /**
     * Returns a {@link android.media.AudioAttributes} from this instance.
     *
     * <p>Field {@link AudioAttributes#allowedCapturePolicy} is ignored for API levels prior to 29.
     */
    @TargetApi(21)
    public android.media.AudioAttributes getAudioAttributesV21() {
        if (audioAttributesV21 == null) {
            android.media.AudioAttributes.Builder builder =
                    new android.media.AudioAttributes.Builder()
                            .setContentType(contentType)
                            .setFlags(flags)
                            .setUsage(usage);
            if (Util.SDK_INT >= 29) {
                builder.setAllowedCapturePolicy(allowedCapturePolicy);
            }
            audioAttributesV21 = builder.build();
        }
        return audioAttributesV21;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AudioAttributes other = (AudioAttributes) obj;
        return this.contentType == other.contentType
                && this.flags == other.flags
                && this.usage == other.usage
                && this.allowedCapturePolicy == other.allowedCapturePolicy;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + contentType;
        result = 31 * result + flags;
        result = 31 * result + usage;
        result = 31 * result + allowedCapturePolicy;
        return result;
    }

}
