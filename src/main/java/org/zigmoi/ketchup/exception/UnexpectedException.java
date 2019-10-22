package org.zigmoi.ketchup.exception;

public class UnexpectedException extends RuntimeException {
    public UnexpectedException(String exceptionMessage) {
        super(exceptionMessage);
    }

    public UnexpectedException(String msg, Exception e) {
        super(msg, e);
    }
}
