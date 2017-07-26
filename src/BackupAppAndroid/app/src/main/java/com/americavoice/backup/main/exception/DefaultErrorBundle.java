package com.americavoice.backup.main.exception;

/**
 * Created by Lesther on 12/17/2016.
 */
/**
 *  Wrapper around Exceptions used to manage default errors.
 */
public class DefaultErrorBundle implements ErrorBundle {

    private static final String DEFAULT_ERROR_MSG = "Unknown error";

    private final Exception mException;

    public DefaultErrorBundle(Exception exception) {
        this.mException = exception;
    }

    @Override
    public Exception getException() {
        return mException;
    }

    @Override
    public String getErrorMessage() {
        return (mException != null) ? this.mException.getMessage() : DEFAULT_ERROR_MSG;
    }
}
