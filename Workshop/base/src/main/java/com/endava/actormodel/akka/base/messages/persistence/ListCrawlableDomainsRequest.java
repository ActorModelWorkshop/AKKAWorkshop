package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;

/**
 * Request the list of domains available for crawl
 */
public class ListCrawlableDomainsRequest extends MessageRequest implements PersistenceRequest {

    /**
     * The default constructor
     */
    public ListCrawlableDomainsRequest() {
        super(System.currentTimeMillis());
    }
}
