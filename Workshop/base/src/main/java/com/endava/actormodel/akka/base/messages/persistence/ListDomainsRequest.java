package com.endava.actormodel.akka.base.messages.persistence;

import com.endava.actormodel.akka.base.messages.MessageRequest;

/**
 * Request the list of all domains.
 */
public class ListDomainsRequest extends MessageRequest implements PersistenceRequest {

    /**
     * The default constructor
     */
    public ListDomainsRequest() {
        super(System.currentTimeMillis());
    }
}
