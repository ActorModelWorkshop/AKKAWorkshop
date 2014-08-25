package com.endava.actormodel.akka.base.exceptions;

public class TimeoutException extends RuntimeException {

    public TimeoutException() {
        super("Timeout");
    }
}
