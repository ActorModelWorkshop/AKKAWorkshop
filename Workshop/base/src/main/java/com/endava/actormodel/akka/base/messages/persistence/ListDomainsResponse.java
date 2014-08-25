package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageResponse;
import com.endava.actormodel.akka.base.entities.Domain;

import java.util.Collections;
import java.util.List;

/**
 * Respond with the list of all domains.
 */
public class ListDomainsResponse extends MessageResponse {

    private final List<Domain> domains;

    public ListDomainsResponse(final ListDomainsRequest listDomainsRequest, final List<Domain> domains) {
        super(System.currentTimeMillis(), listDomainsRequest);

        this.domains = Collections.unmodifiableList(domains);
    }

    public List<Domain> getDomains() {
        return domains;
    }
}
