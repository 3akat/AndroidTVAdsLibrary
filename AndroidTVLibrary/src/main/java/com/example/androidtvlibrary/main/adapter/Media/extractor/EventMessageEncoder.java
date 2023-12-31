package com.example.androidtvlibrary.main.adapter.Media.extractor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class EventMessageEncoder {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final DataOutputStream dataOutputStream;

    public EventMessageEncoder() {
        byteArrayOutputStream = new ByteArrayOutputStream(512);
        dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    }

    public byte[] encode(EventMessage eventMessage) {
        byteArrayOutputStream.reset();
        try {
            writeNullTerminatedString(dataOutputStream, eventMessage.schemeIdUri);
            String nonNullValue = eventMessage.value != null ? eventMessage.value : "";
            writeNullTerminatedString(dataOutputStream, nonNullValue);
            writeUnsignedInt(dataOutputStream, eventMessage.durationMs);
            writeUnsignedInt(dataOutputStream, eventMessage.id);
            dataOutputStream.write(eventMessage.messageData);
            dataOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
    }

    private static void writeNullTerminatedString(DataOutputStream dataOutputStream, String value)
            throws IOException {
        dataOutputStream.writeBytes(value);
        dataOutputStream.writeByte(0);
    }

    private static void writeUnsignedInt(DataOutputStream outputStream, long value)
            throws IOException {
        outputStream.writeByte((int) (value >>> 24) & 0xFF);
        outputStream.writeByte((int) (value >>> 16) & 0xFF);
        outputStream.writeByte((int) (value >>> 8) & 0xFF);
        outputStream.writeByte((int) value & 0xFF);
    }

}
