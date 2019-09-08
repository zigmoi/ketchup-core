package org.zigmoi.ketchup.iam.authz.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAclException extends RuntimeException {
    public InvalidAclException(String exception) {
        super(exception);
    }
}