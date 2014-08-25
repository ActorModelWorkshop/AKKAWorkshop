package com.endava.actormodel.akka.base.messages;

import java.io.Serializable;

public abstract class MessageRequest implements Serializable {

    private final long id;

    public MessageRequest(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
