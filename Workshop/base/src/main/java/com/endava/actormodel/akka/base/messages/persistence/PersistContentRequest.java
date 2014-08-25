package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;
import com.endava.actormodel.akka.base.entities.Link;

/**
 * Request to persist the cleaned content of a page
 */
public class PersistContentRequest extends MessageRequest implements PersistenceRequest {

    private final Link link;
    private final String cleanedContent;

    public PersistContentRequest(final Link link, final String cleanedContent) {
        super(System.currentTimeMillis());

        this.link = link;
        this.cleanedContent = cleanedContent;
    }

    public Link getLink() {
        return link;
    }

    public String getCleanedContent() {
        return cleanedContent;
    }
}
