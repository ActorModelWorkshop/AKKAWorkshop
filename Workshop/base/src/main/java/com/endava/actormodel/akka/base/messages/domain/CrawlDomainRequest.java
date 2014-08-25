package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.messages.MessageRequest;
import com.endava.actormodel.akka.base.entities.Domain;

public class CrawlDomainRequest extends MessageRequest {

    private final Domain domain;

    public CrawlDomainRequest(final Domain domain) {
        super(System.currentTimeMillis());

        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
