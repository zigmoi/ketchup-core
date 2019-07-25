package org.zigmoi.ketchup.exception;

public class KConfigurationException extends RuntimeException {
    public KConfigurationException(String exceptionMessage) {
        super(exceptionMessage);
    }
    public KConfigurationException(String s, Exception ex) {
        super(s, ex);
    }
}
