package io.roa.secretmanger.Exception;

public class QuorumNotReachedException extends RuntimeException {
    public QuorumNotReachedException(String message) { super(message); }
}
