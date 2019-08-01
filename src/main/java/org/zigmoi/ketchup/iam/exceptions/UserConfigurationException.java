package org.zigmoi.ketchup.iam.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserConfigurationException extends RuntimeException {
    public UserConfigurationException(String exception) {
        super(exception);
    }
}