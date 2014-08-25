package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageResponse;
import com.endava.actormodel.akka.base.entities.Domain;

import java.util.Collections;
import java.util.List;

/**
 * Respond with the list of domains to crawl
 */
public class ListCrawlableDomainsResponse extends MessageResponse {

    private final List<Domain> crawlableDomains;

    public ListCrawlableDomainsResponse(final ListCrawlableDomainsRequest listCrawlableDomainsRequest,
                                        final List<Domain> crawlableDomains) {
        super(System.currentTimeMillis(), listCrawlableDomainsRequest);

        this.crawlableDomains = Collections.unmodifiableList(crawlableDomains);
    }

    public List<Domain> getCrawlableDomains() {
        return crawlableDomains;
    }
}
