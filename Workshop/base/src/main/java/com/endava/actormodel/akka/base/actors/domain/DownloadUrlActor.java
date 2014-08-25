package com.endava.actormodel.akka.base.actors.domain;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlRequest;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlResponse;
import com.endava.actormodel.akka.base.messages.persistence.UpdateLinkRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessContentRequest;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.LinkStatus;
import com.endava.actormodel.akka.base.tools.WebClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import scala.Option;

import java.io.IOException;
import java.util.Map;

public class DownloadUrlActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(final Object message) throws IOException {
        if (message instanceof DownloadUrlRequest) {
            processDownloadUrlRequest((DownloadUrlRequest) message);

        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void processDownloadUrlRequest(final DownloadUrlRequest downloadUrlRequest) throws IOException {
        /* Validate requested link by retrieving page headers and checking: response code, mime type */
        final Map<String, String> pageHeaders = WebClient.getPageHeaders(downloadUrlRequest.getLink().getUrl());

        if (!pageHeaders.get(CrawlerConstants.HTTP_CUSTOM_HEADER_RESPONSE_CODE).equals(CrawlerConstants.HTTP_RESPONSE_CODE_OK)) {
            LOG.debug(String.format("%s - Response code not accepted: %s", downloadUrlRequest.getLink().getUrl(),
                    pageHeaders.get(CrawlerConstants.HTTP_CUSTOM_HEADER_RESPONSE_CODE)));

            reportWork(downloadUrlRequest, LinkStatus.FAILED);
            return;
        }

        if (!WebClient.isMediaTypeAccepted(pageHeaders.get(CrawlerConstants.HTTP_HEADER_CONTENT_TYPE))) {
            LOG.debug(String.format("%s - Media type not accepted: %s", downloadUrlRequest.getLink().getUrl(),
                    pageHeaders.get(CrawlerConstants.HTTP_HEADER_CONTENT_TYPE)));

            reportWork(downloadUrlRequest, LinkStatus.VISITED);
            return;
        }

        doWork(downloadUrlRequest);
        reportWork(downloadUrlRequest, LinkStatus.VISITED);
    }

    private void doWork(final DownloadUrlRequest downloadUrlRequest) throws IOException {
        /* Get page content */
        final String pageContent = WebClient.getPageContent(downloadUrlRequest.getLink().getUrl());

        LOG.info(String.format("%s - Downloaded content of %d chars", downloadUrlRequest.getLink().getUrl(),
                pageContent.length()));

        /* Send to processing master */
        getSender().tell(new ProcessContentRequest(downloadUrlRequest.getDomain(), downloadUrlRequest.getLink(), pageContent), getSelf());
    }

    /**
     * Finish the download actor work: persist link status; send the download url response
     */
    private void reportWork(final DownloadUrlRequest request, final LinkStatus urlStatus, final boolean unresponsiveDomain) {
        /* Persist the new link status */
        final Link newLink = new Link(request.getLink(), urlStatus);
        getSender().tell(new UpdateLinkRequest(newLink), getSelf());

        /* Report back to the domain actor */
        final DownloadUrlResponse response = new DownloadUrlResponse(request, unresponsiveDomain);
        getSender().tell(response, getSelf());
    }

    private void reportWork(final DownloadUrlRequest request, final LinkStatus urlStatus) {
        reportWork(request, urlStatus, false);
    }

    @Override
    public void preRestart(final Throwable reason, final Option<Object> message) throws Exception {
        final DownloadUrlRequest request = (DownloadUrlRequest) message.get();
        if (reason instanceof ClientProtocolException) {
            reportWork(request, LinkStatus.FAILED);
        } else if (reason instanceof ConnectTimeoutException) {
            reportWork(request, LinkStatus.FAILED, true);
        } else if (reason instanceof IOException) {
            reportWork(request, LinkStatus.FAILED, true);
        }

        super.preRestart(reason, message);
    }
}