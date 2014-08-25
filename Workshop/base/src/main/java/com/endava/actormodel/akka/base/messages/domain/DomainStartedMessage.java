package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.entities.Domain;

public class DomainStartedMessage {

    private final Domain domain;

    public DomainStartedMessage(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
