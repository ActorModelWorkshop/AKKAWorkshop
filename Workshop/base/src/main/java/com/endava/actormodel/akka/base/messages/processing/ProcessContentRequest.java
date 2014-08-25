package com.endava.actormodel.akka.base.messages.processing;

import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.messages.MessageRequest;

public class ProcessContentRequest extends MessageRequest implements ProcessingRequest {

    private final Domain sourceDomain;
    private final Link sourceLink;
    private final String content;

    public ProcessContentRequest(final Domain sourceDomain, final Link sourceLink, final String content) {
        super(System.currentTimeMillis());

        this.sourceDomain = sourceDomain;
        this.sourceLink = sourceLink;
        this.content = content;
    }

    public Domain getSourceDomain() {
        return sourceDomain;
    }

    public Link getSourceLink() {
        return sourceLink;
    }

    public String getContent() {
        return content;
    }
}
