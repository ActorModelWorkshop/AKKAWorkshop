package com.endava.actormodel.akka.base.messages;

import java.io.Serializable;

public abstract class MessageResponse implements Serializable {

    private final long id;
    private final MessageRequest messageRequest;

    public MessageResponse(final long id, final MessageRequest messageRequest) {
        this.id = id;
        this.messageRequest = messageRequest;
    }

    public long getId() {
        return id;
    }

    public MessageRequest getMessageRequest() {
        return messageRequest;
    }
}
