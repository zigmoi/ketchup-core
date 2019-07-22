package org.zigmoi.ketchup.test.git;

import org.apache.maven.shared.utils.cli.CommandLineException;

public class MException {
    private int errorCode;
    private CommandLineException exception;

    public CommandLineException getException() {
        return exception;
    }

    public void setException(CommandLineException exception) {
        this.exception = exception;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
