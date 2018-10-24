package com.revolut.task.exception;

import javax.ws.rs.core.Response;

/**
 * Created by Alexey on 21.10.2018.
 */
public class TransferErrorType implements Response.StatusType {

    public TransferErrorType(final Response.Status.Family family, final int statusCode,
                             final String reasonPhrase) {
        super();
        this.family = family;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    protected TransferErrorType(final Response.Status status,
                                final String reasonPhrase) {
        this(status.getFamily(), status.getStatusCode(), reasonPhrase);
    }

    protected TransferErrorType(final TransferErrorType.Status status,
                                final String reasonPhrase) {
        this(status.getFamily(), status.getStatusCode(), reasonPhrase);
    }

    @Override
    public Response.Status.Family getFamily() { return family; }

    @Override
    public String getReasonPhrase() { return reasonPhrase; }

    @Override
    public int getStatusCode() { return statusCode; }

    private final Response.Status.Family family;
    private final int statusCode;
    private final String reasonPhrase;

    public enum Status implements Response.StatusType {
        DIFFERENT_CURRENCY(430, "Currency of the accounts does not matches"),
        NOT_ENOUGH_MONEY(431, "there is not enough money in the account to complete the transaction");

        private final int code;
        private final String reason;
        private final Response.Status.Family family;

        Status(int statusCode, String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            this.family = Response.Status.Family.familyOf(statusCode);
        }

        public Response.Status.Family getFamily() {
            return this.family;
        }

        public int getStatusCode() {
            return this.code;
        }

        public String getReasonPhrase() {
            return this.toString();
        }

        public String toString() {
            return this.reason;
        }

    }

}

