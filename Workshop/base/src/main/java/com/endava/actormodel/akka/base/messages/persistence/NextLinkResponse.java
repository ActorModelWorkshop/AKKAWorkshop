package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageResponse;
import com.endava.actormodel.akka.base.entities.Link;

/**
 * Send the next link to be crawled.
 */
public class NextLinkResponse extends MessageResponse {

    private final Link nextLink;

    public NextLinkResponse(final NextLinkRequest nextLinkRequest, final Link nextLink) {
        super(System.currentTimeMillis(), nextLinkRequest);

        this.nextLink = nextLink;
    }

    public NextLinkRequest getNextLinkRequest() {
        return (NextLinkRequest) getMessageRequest();
    }

    public Link getNextLink() {
        return nextLink;
    }
}
