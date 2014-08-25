package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.messages.MessageRequest;

public class DownloadUrlRequest extends MessageRequest {

    private final Domain domain;
    private final Link link;

    public DownloadUrlRequest(final Domain domain, final Link link) {
        super(System.currentTimeMillis());

        this.domain = domain;
        this.link = link;
    }

    public Link getLink() {
        return link;
    }

    public Domain getDomain() {
        return domain;
    }
}
