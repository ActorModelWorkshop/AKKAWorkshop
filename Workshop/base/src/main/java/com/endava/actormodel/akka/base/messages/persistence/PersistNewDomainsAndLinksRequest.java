package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;

import java.util.List;

/**
 * Request to persist lists of domains and links
 */
public class PersistNewDomainsAndLinksRequest extends MessageRequest implements PersistenceRequest {

    private final List<Domain> newDomains;
    private final List<Link> newLinks;

    public PersistNewDomainsAndLinksRequest(final List<Domain> newDomains, final List<Link> newLinks) {
        super(System.currentTimeMillis());

        this.newDomains = newDomains;
        this.newLinks = newLinks;
    }

    public List<Domain> getNewDomains() {
        return newDomains;
    }

    public List<Link> getNewLinks() {
        return newLinks;
    }
}
