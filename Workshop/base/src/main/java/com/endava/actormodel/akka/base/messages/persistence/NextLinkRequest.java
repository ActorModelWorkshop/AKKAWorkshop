package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;
import com.endava.actormodel.akka.base.entities.Domain;

/**
 * Request a new link to crawl.
 */
public class NextLinkRequest extends MessageRequest implements PersistenceRequest {

    private final Domain domain;

    public NextLinkRequest(final Domain domain) {
        super(System.currentTimeMillis());

        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }
}
