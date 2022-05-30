package me.lusory.ostrich.qapi.exceptions;

import java.io.IOException;

public class QAPIException extends IOException {
    public QAPIException() {
        this(null, null);
    }

    public QAPIException(final String message) {
        this(message, null);
    }

    public QAPIException(final Throwable cause) {
        this(cause != null ? cause.getMessage() : null, cause);
    }

    public QAPIException(final String message, final Throwable cause) {
        super(message);
        if (cause != null) {
            super.initCause(cause);
        }
    }
}
