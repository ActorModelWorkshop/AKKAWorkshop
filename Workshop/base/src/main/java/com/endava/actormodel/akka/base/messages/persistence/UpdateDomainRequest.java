package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.entities.Domain;

public class UpdateDomainRequest implements PersistenceRequest {

    private final Domain domain;

    public UpdateDomainRequest(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
