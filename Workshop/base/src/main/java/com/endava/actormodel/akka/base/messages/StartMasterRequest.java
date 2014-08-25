package com.endava.actormodel.akka.base.messages;

public class StartMasterRequest extends MessageRequest {

    public StartMasterRequest() {
        super(System.currentTimeMillis());
    }
}
