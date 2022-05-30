package me.lusory.ostrich.qapi.exceptions;

public class QAPISocketException extends RuntimeException {
    public QAPISocketException() {
        this(null, null);
    }

    public QAPISocketException(final String message) {
        this(message, null);
    }

    public QAPISocketException(final Throwable cause) {
        this(cause != null ? cause.getMessage() : null, cause);
    }

    public QAPISocketException(final String message, final Throwable cause) {
        super(message);
        if (cause != null) {
            super.initCause(cause);
        }
    }
}
