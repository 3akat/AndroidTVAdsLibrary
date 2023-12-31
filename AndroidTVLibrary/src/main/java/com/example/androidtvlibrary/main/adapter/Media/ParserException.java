package com.example.androidtvlibrary.main.adapter.Media;

import java.io.IOException;

public class ParserException extends IOException {

    public ParserException() {
        super();
    }

    /**
     * @param message The detail message for the exception.
     */
    public ParserException(String message) {
        super(message);
    }

    /**
     * @param cause The cause for the exception.
     */
    public ParserException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message The detail message for the exception.
     * @param cause The cause for the exception.
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
