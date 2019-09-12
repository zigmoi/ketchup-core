package org.zigmoi.ketchup.exception;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String exceptionMessage) {
        super(exceptionMessage);
    }
    public ConfigurationException(String s, Exception ex) {
        super(s, ex);
    }
}
