package com.endava.actormodel.akka.base.exceptions;

import com.endava.actormodel.akka.base.entities.Domain;

/**
 * Exception thrown when there are no more links to download for the domain.
 */
public class ExhaustedDomainException extends Exception {

    private Domain domain;

    public ExhaustedDomainException(Domain domain) {
        super();
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }

}
