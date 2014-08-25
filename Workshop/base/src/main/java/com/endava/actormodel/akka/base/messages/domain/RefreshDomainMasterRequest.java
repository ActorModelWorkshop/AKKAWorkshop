package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.messages.MessageRequest;

/**
 * Request a DomainMaster refresh of domains
 */
public class RefreshDomainMasterRequest extends MessageRequest {

    public RefreshDomainMasterRequest() {
        super(System.currentTimeMillis());
    }
}
