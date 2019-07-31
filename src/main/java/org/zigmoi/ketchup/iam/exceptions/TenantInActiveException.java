package org.zigmoi.ketchup.iam.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantInActiveException extends RuntimeException {
    public TenantInActiveException(String exception) {
        super(exception);
    }
}