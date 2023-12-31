package com.example.androidtvlibrary.main.adapter;

import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.util.Assertions;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Defines a region of data.
 */
public final class DataSpecTest {

    /**
     * The flags that apply to any request for data. Possible flag values are {@link
     * #FLAG_ALLOW_GZIP}, {@link #FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN} and {@link
     * #FLAG_ALLOW_CACHE_FRAGMENTATION}.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            flag = true,
            value = {FLAG_ALLOW_GZIP, FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN, FLAG_ALLOW_CACHE_FRAGMENTATION})
    public @interface Flags {
    }


    public static final int FLAG_ALLOW_GZIP = 1;
    /**
     * Prevents caching if the length cannot be resolved when the {@link DataSource} is opened.
     */
    public static final int FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN = 1 << 1; // 2
    /**
     * Allows fragmentation of this request into multiple cache files, meaning a cache eviction policy
     * will be able to evict individual fragments of the data. Depending on the cache implementation,
     * setting this flag may also enable more concurrent access to the data (e.g. reading one fragment
     * whilst writing another).
     */
    public static final int FLAG_ALLOW_CACHE_FRAGMENTATION = 1 << 2; // 4


    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HTTP_METHOD_GET, HTTP_METHOD_POST, HTTP_METHOD_HEAD})
    public @interface HttpMethod {
    }

    public static final int HTTP_METHOD_GET = 1;
    public static final int HTTP_METHOD_POST = 2;
    public static final int HTTP_METHOD_HEAD = 3;

    /**
     * The source from which data should be read.
     */
    public final Uri uri;

    public final @HttpMethod int httpMethod;

    /**
     * The HTTP request body, null otherwise. If the body is non-null, then httpBody.length will be
     * non-zero.
     */
    @Nullable
    public final byte[] httpBody;

    /**
     * Immutable map containing the headers to use in HTTP requests.
     */
    public final Map<String, String> httpRequestHeaders;

    /**
     * The absolute position of the data in the full stream.
     */
    public final long absoluteStreamPosition;
    /**
     * The position of the data when read from {@link #uri}.
     * <p>
     * Always equal to {@link #absoluteStreamPosition} unless the {@link #uri} defines the location
     * of a subset of the underlying data.
     */
    public final long position;
    /**
     * The length of the data, or {@link C#LENGTH_UNSET}.
     */
    public final long length;
    /**
     * A key that uniquely identifies the original stream. Used for cache indexing. May be null if the
     * data spec is not intended to be used in conjunction with a cache.
     */
    @Nullable
    public final String key;
    /**
     * Request {@link Flags flags}.
     */
    public final @Flags int flags;

    /**
     * Construct a data spec for the given uri and with {@link #key} set to null.
     *
     * @param uri {@link #uri}.
     */
    public DataSpecTest(Uri uri) {
        this(uri, 0);
    }

    /**
     * Construct a data spec for the given uri and with {@link #key} set to null.
     *
     * @param uri   {@link #uri}.
     * @param flags {@link #flags}.
     */
    public DataSpecTest(Uri uri, @Flags int flags) {
        this(uri, 0, C.LENGTH_UNSET, null, flags);
    }

    /**
     * Construct a data spec where {@link #position} equals {@link #absoluteStreamPosition}.
     *
     * @param uri                    {@link #uri}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}, equal to {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     */
    public DataSpecTest(Uri uri, long absoluteStreamPosition, long length, @Nullable String key) {
        this(uri, absoluteStreamPosition, absoluteStreamPosition, length, key, 0);
    }

    /**
     * Construct a data spec where {@link #position} equals {@link #absoluteStreamPosition}.
     *
     * @param uri                    {@link #uri}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}, equal to {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     */
    public DataSpecTest(
            Uri uri, long absoluteStreamPosition, long length, @Nullable String key, @Flags int flags) {
        this(uri, absoluteStreamPosition, absoluteStreamPosition, length, key, flags);
    }

    /**
     * Construct a data spec where {@link #position} equals {@link #absoluteStreamPosition} and has
     * request headers.
     *
     * @param uri                    {@link #uri}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}, equal to {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     * @param httpRequestHeaders     {@link #httpRequestHeaders}
     */
    public DataSpecTest(
            Uri uri,
            long absoluteStreamPosition,
            long length,
            @Nullable String key,
            @Flags int flags,
            Map<String, String> httpRequestHeaders) {
        this(
                uri,
                inferHttpMethod(null),
                null,
                absoluteStreamPosition,
                absoluteStreamPosition,
                length,
                key,
                flags,
                httpRequestHeaders);
    }

    /**
     * Construct a data spec where {@link #position} may differ from {@link #absoluteStreamPosition}.
     *
     * @param uri                    {@link #uri}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
     * @param position               {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     */
    public DataSpecTest(
            Uri uri,
            long absoluteStreamPosition,
            long position,
            long length,
            @Nullable String key,
            @Flags int flags) {
        this(uri, null, absoluteStreamPosition, position, length, key, flags);
    }

    /**
     * Construct a data spec by inferring the {@link #httpMethod} based on the {@code postBody}
     * parameter. If postBody is non-null, then httpMethod is set to {@link #HTTP_METHOD_POST}. If
     * postBody is null, then httpMethod is set to {@link #HTTP_METHOD_GET}.
     *
     * @param uri                    {@link #uri}.
     * @param postBody               {@link #httpBody} The body of the HTTP request, which is also used to infer the
     *                               {@link #httpMethod}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
     * @param position               {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     */
    public DataSpecTest(
            Uri uri,
            @Nullable byte[] postBody,
            long absoluteStreamPosition,
            long position,
            long length,
            @Nullable String key,
            @Flags int flags) {
        this(
                uri,
                /* httpMethod= */ inferHttpMethod(postBody),
                /* httpBody= */ postBody,
                absoluteStreamPosition,
                position,
                length,
                key,
                flags);
    }

    /**
     * Construct a data spec where {@link #position} may differ from {@link #absoluteStreamPosition}.
     *
     * @param uri                    {@link #uri}.
     * @param httpMethod             {@link #httpMethod}.
     * @param httpBody               {@link #httpBody}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
     * @param position               {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     */
    public DataSpecTest(
            Uri uri,
            @HttpMethod int httpMethod,
            @Nullable byte[] httpBody,
            long absoluteStreamPosition,
            long position,
            long length,
            @Nullable String key,
            @Flags int flags) {
        this(
                uri,
                httpMethod,
                httpBody,
                absoluteStreamPosition,
                position,
                length,
                key,
                flags,
                /* httpRequestHeaders= */ Collections.emptyMap());
    }

    /**
     * Construct a data spec with request parameters to be used as HTTP headers inside HTTP requests.
     *
     * @param uri                    {@link #uri}.
     * @param httpMethod             {@link #httpMethod}.
     * @param httpBody               {@link #httpBody}.
     * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
     * @param position               {@link #position}.
     * @param length                 {@link #length}.
     * @param key                    {@link #key}.
     * @param flags                  {@link #flags}.
     * @param httpRequestHeaders     {@link #httpRequestHeaders}.
     */
    public DataSpecTest(
            Uri uri,
            @HttpMethod int httpMethod,
            @Nullable byte[] httpBody,
            long absoluteStreamPosition,
            long position,
            long length,
            @Nullable String key,
            @Flags int flags,
            Map<String, String> httpRequestHeaders) {
        checkArgument(absoluteStreamPosition >= 0);
        checkArgument(position >= 0);
        checkArgument(length > 0 || length == C.LENGTH_UNSET);
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.httpBody = (httpBody != null && httpBody.length != 0) ? httpBody : null;
        this.absoluteStreamPosition = absoluteStreamPosition;
        this.position = position;
        this.length = length;
        this.key = key;
        this.flags = flags;
        this.httpRequestHeaders = Collections.unmodifiableMap(new HashMap<>(httpRequestHeaders));
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns whether the given flag is set.
     *
     * @param flag Flag to be checked if it is set.
     */
    public boolean isFlagSet(@Flags int flag) {
        return (this.flags & flag) == flag;
    }

    @Override
    public String toString() {
        return "DataSpec["
                + getHttpMethodString()
                + " "
                + uri
                + ", "
                + Arrays.toString(httpBody)
                + ", "
                + absoluteStreamPosition
                + ", "
                + position
                + ", "
                + length
                + ", "
                + key
                + ", "
                + flags
                + "]";
    }

    /**
     * Returns an uppercase HTTP method name (e.g., "GET", "POST", "HEAD") corresponding to the {@link
     * #httpMethod}.
     */
    public final String getHttpMethodString() {
        return getStringForHttpMethod(httpMethod);
    }

    /**
     * Returns an uppercase HTTP method name (e.g., "GET", "POST", "HEAD") corresponding to the {@code
     * httpMethod}.
     */
    public static String getStringForHttpMethod(@HttpMethod int httpMethod) {
        switch (httpMethod) {
            case HTTP_METHOD_GET:
                return "GET";
            case HTTP_METHOD_POST:
                return "POST";
            case HTTP_METHOD_HEAD:
                return "HEAD";
            default:
                throw new AssertionError(httpMethod);
        }
    }

    /**
     * Returns a data spec that represents a subrange of the data defined by this DataSpec. The
     * subrange includes data from the offset up to the end of this DataSpec.
     *
     * @param offset The offset of the subrange.
     * @return A data spec that represents a subrange of the data defined by this DataSpec.
     */
    public DataSpecTest subrange(long offset) {
        return subrange(offset, length == C.LENGTH_UNSET ? C.LENGTH_UNSET : length - offset);
    }

    /**
     * Returns a data spec that represents a subrange of the data defined by this DataSpec.
     *
     * @param offset The offset of the subrange.
     * @param length The length of the subrange.
     * @return A data spec that represents a subrange of the data defined by this DataSpec.
     */
    public DataSpecTest subrange(long offset, long length) {
        if (offset == 0 && this.length == length) {
            return this;
        } else {
            return new DataSpecTest(
                    uri,
                    httpMethod,
                    httpBody,
                    absoluteStreamPosition + offset,
                    position + offset,
                    length,
                    key,
                    flags,
                    httpRequestHeaders);
        }
    }

    /**
     * Returns a copy of this data spec with the specified Uri.
     *
     * @param uri The new source {@link Uri}.
     * @return The copied data spec with the specified Uri.
     */
    public DataSpecTest withUri(Uri uri) {
        return new DataSpecTest(
                uri,
                httpMethod,
                httpBody,
                absoluteStreamPosition,
                position,
                length,
                key,
                flags,
                httpRequestHeaders);
    }

    /**
     * Returns a copy of this data spec with the specified request headers.
     *
     * @param requestHeaders The HTTP request headers.
     * @return The copied data spec with the specified request headers.
     */
    public DataSpecTest withRequestHeaders(Map<String, String> requestHeaders) {
        return new DataSpecTest(
                uri,
                httpMethod,
                httpBody,
                absoluteStreamPosition,
                position,
                length,
                key,
                flags,
                requestHeaders);
    }

    /**
     * Returns a copy this data spec with additional request headers.
     *
     * <p>Note: Values in {@code requestHeaders} will overwrite values with the same header key that
     * were previously set in this instance's {@code #httpRequestHeaders}.
     *
     * @param requestHeaders The additional HTTP request headers.
     * @return The copied data with the additional HTTP request headers.
     */
    public DataSpecTest withAdditionalHeaders(Map<String, String> requestHeaders) {
        Map<String, String> totalHeaders = new HashMap<>(this.httpRequestHeaders);
        totalHeaders.putAll(requestHeaders);

        return new DataSpecTest(
                uri,
                httpMethod,
                httpBody,
                absoluteStreamPosition,
                position,
                length,
                key,
                flags,
                totalHeaders);
    }

    @HttpMethod
    private static int inferHttpMethod(@Nullable byte[] postBody) {
        return postBody != null ? HTTP_METHOD_POST : HTTP_METHOD_GET;
    }
}

