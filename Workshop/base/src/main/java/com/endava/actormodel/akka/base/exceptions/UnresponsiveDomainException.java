package com.endava.actormodel.akka.base.exceptions;

import com.endava.actormodel.akka.base.entities.Domain;

/**
 * Exception thrown when the domain doesn't respond for a specified amount of times.
 */
public class UnresponsiveDomainException extends Exception {

    private Domain domain;

    public UnresponsiveDomainException(Domain domain) {
        super();
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
