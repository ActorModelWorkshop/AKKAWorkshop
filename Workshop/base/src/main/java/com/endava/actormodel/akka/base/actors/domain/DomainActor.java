package com.endava.actormodel.akka.base.actors.domain;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.exceptions.TimeoutException;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlRequest;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlResponse;
import com.endava.actormodel.akka.base.messages.domain.*;
import com.endava.actormodel.akka.base.messages.persistence.NextLinkRequest;
import com.endava.actormodel.akka.base.messages.persistence.NextLinkResponse;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;
import com.endava.actormodel.akka.base.messages.persistence.UpdateDomainRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessingRequest;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import com.endava.actormodel.akka.base.exceptions.ExhaustedDomainException;
import com.endava.actormodel.akka.base.exceptions.UnresponsiveDomainException;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Domain controller actor
 */
public class DomainActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private int noOfConsecutiveFails;
    private int noOfConsecutiveEmptyNextLink;

    private final ActorRef parent;
    private final Domain domain;

    /**
     * The constructor
     *
     * @param parent the actor that created the domain actor
     */
    public DomainActor(final ActorRef parent, final Domain domain) {
        this.parent = parent;
        this.domain = domain;
    }

    @Override
    public void onReceive(Object message) throws UnresponsiveDomainException, ExhaustedDomainException {
        if (message instanceof CrawlDomainRequest) {
            getParent().tell(new NextLinkRequest(((CrawlDomainRequest) message).getDomain()), getSelf());

        } else if (message instanceof NextLinkResponse) {
            crawlNextLink((NextLinkResponse) message);

        } else if (message instanceof DownloadUrlResponse) {
            processLinkDownload((DownloadUrlResponse) message);

        } else if (message instanceof PersistenceRequest) {
            getParent().tell(message, getSelf());
        } else if (message instanceof ProcessingRequest) {
            getParent().tell(message, getSelf());
//        } else if (message instanceof ReceiveTimeout) {
//            throw new TimeoutException();
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void crawlNextLink(final NextLinkResponse nextLinkResponse) throws ExhaustedDomainException {
        final NextLinkRequest nextLinkRequest = nextLinkResponse.getNextLinkRequest();

        if (null == nextLinkResponse.getNextLink()) {
            LOG.info("Domain " + nextLinkRequest.getDomain().getName() + " has no more links to crawl");

            boolean limitReached = ++noOfConsecutiveEmptyNextLink == CrawlerConstants.EMPTY_NEXT_LINK_TRIALS;
            if (limitReached) {
                throw new ExhaustedDomainException(nextLinkRequest.getDomain());
            }

            /* Schedule a new crawl for the downloaded domain after the cool down period */
            getContext().system().scheduler().scheduleOnce(Duration.create(nextLinkRequest.getDomain().getCoolDownPeriod(),
                    TimeUnit.MILLISECONDS), getSelf(), new CrawlDomainRequest(nextLinkRequest.getDomain()), getContext().system().dispatcher(), getSelf());
            return;
        }

        LOG.info("Domain " + nextLinkResponse.getNextLinkRequest().getDomain().getName() + " crawling link: " + nextLinkResponse.getNextLink().getUrl());

        noOfConsecutiveEmptyNextLink = 0;

        /* Send a "download URL" request */
        getParent().tell(new DownloadUrlRequest(nextLinkRequest.getDomain(), nextLinkResponse.getNextLink()), getSelf());
    }

    private void processLinkDownload(final DownloadUrlResponse downloadUrlResponse) throws UnresponsiveDomainException {
        final Domain domain = downloadUrlResponse.getDownloadUrlRequest().getDomain();

        if (downloadUrlResponse.isUnresponsiveDomain()) {
            boolean limitReached = ++noOfConsecutiveFails == CrawlerConstants.CONNECTION_EXCEPTION_TRIALS;
            if (limitReached) {
                throw new UnresponsiveDomainException(domain);
            }
        } else {
            noOfConsecutiveFails = 0;
        }

        /* Schedule a new crawl for the downloaded domain after the cool down period */
        getContext().system().scheduler().scheduleOnce(Duration.create(domain.getCoolDownPeriod(), TimeUnit.MILLISECONDS), getSelf(),
                new CrawlDomainRequest(domain), getContext().system().dispatcher(), getSelf());
    }

    @Override
    public void postStop() throws Exception {
        /* Remove the actor from the DomainMaster's map after it is stopped */
        final Domain stoppedDomain = new Domain(domain, DomainStatus.STOPPED);

        getParent().tell(new DomainStoppedMessage(domain), getSelf());
        getParent().tell(new UpdateDomainRequest(stoppedDomain), getSelf());

        super.postStop();
    }

    @Override
    public void preStart() throws Exception {
        /* Add the actor to the DomainMaster's map after it is started */
        final Domain startedDomain = new Domain(domain, DomainStatus.STARTED);

        getParent().tell(new DomainStartedMessage(startedDomain), getSelf());
        getParent().tell(new UpdateDomainRequest(startedDomain), getSelf());

//        getContext().setReceiveTimeout(Duration.create(getDomain().getCoolDownPeriod() * 2, TimeUnit.MILLISECONDS));

        super.preStart();
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        getSelf().tell(new CrawlDomainRequest(domain), getSelf());
    }

    public ActorRef getParent() {
        return parent;
    }

    public Domain getDomain() {
        return domain;
    }
}