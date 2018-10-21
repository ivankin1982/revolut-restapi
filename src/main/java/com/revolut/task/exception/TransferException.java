package com.revolut.task.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by Alexey on 21.10.2018.
 */
public class TransferException extends WebApplicationException {
    public TransferException(String message) {
        super(Response.status(new TransferErrorType(Response.Status.BAD_REQUEST, message)).build());
    }

    public TransferException(TransferErrorType.Status status, String message) {
        super(Response.status(new TransferErrorType(status, message)).build());
    }
}
