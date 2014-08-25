package com.endava.actormodel.akka.base.messages.domain;

import com.endava.actormodel.akka.base.messages.MessageResponse;

public class DownloadUrlResponse extends MessageResponse {

    private final boolean unresponsiveDomain;

    public DownloadUrlResponse(final DownloadUrlRequest downloadUrlRequest, boolean unresponsiveDomain) {
        super(System.currentTimeMillis(), downloadUrlRequest);
        this.unresponsiveDomain = unresponsiveDomain;
    }

    public DownloadUrlResponse(final DownloadUrlRequest downloadUrlRequest) {
        super(System.currentTimeMillis(), downloadUrlRequest);
        this.unresponsiveDomain = false;
    }

    public DownloadUrlRequest getDownloadUrlRequest() {
        return (DownloadUrlRequest) getMessageRequest();
    }

    public boolean isUnresponsiveDomain() {
        return unresponsiveDomain;
    }
}
