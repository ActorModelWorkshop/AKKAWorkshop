package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;

/**
 * Request to save/update a link.
 */
public class UpdateLinkRequest extends MessageRequest implements PersistenceRequest {

    private final Link link;
    private final String source;

    public UpdateLinkRequest(Link link, String source) {
        super(System.currentTimeMillis());
        this.link = link;
        this.source = source;
    }

    public UpdateLinkRequest(Link link) {
        this(link, null);
    }

    public Link getLink() {
        return link;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "UpdateLinkRequest{" +
                "link=" + link +
                ", source='" + source + '\'' +
                '}';
    }
}
