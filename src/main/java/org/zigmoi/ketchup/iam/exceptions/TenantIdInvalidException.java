package org.zigmoi.ketchup.iam.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TenantIdInvalidException extends RuntimeException {
    public TenantIdInvalidException(String exception) {
        super(exception);
    }
}
