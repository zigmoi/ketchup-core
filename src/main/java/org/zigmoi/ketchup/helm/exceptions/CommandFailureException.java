package org.zigmoi.ketchup.helm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CommandFailureException extends Exception {
    public CommandFailureException(String message) {
        super(message);
    }

    public CommandFailureException(String message, Exception e) {
        super(message,e);
    }

}