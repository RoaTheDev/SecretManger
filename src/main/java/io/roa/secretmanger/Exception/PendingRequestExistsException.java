package io.roa.secretmanger.Exception;

public class PendingRequestExistsException extends RuntimeException {
    public PendingRequestExistsException(String message) { super(message); }
}