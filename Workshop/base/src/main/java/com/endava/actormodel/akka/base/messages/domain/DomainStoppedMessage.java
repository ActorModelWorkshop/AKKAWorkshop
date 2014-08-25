package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.entities.Domain;

public class DomainStoppedMessage {

    private final Domain domain;

    public DomainStoppedMessage(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
