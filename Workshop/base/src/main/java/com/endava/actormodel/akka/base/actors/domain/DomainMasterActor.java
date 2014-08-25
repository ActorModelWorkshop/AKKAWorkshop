package com.endava.actormodel.akka.base.actors.domain;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlRequest;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlResponse;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;
import com.endava.actormodel.akka.base.messages.domain.*;
import com.endava.actormodel.akka.base.messages.persistence.*;
import com.endava.actormodel.akka.base.messages.processing.ProcessingRequest;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import com.endava.actormodel.akka.base.exceptions.ExhaustedDomainException;
import com.endava.actormodel.akka.base.exceptions.UnresponsiveDomainException;
import scala.concurrent.duration.Duration;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Domains master actor
 */
public class DomainMasterActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final Map<String, ActorRef> domainActors;

    private final Map<String, Long> unresponsiveDomains;
    private final Map<String, Long> exhaustedDomains;

    private final ActorRef parent; // MasterActor

    private final SupervisorStrategy supervisorStrategy = new OneForOneStrategy(-1, Duration.create(1, TimeUnit.MINUTES),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                @Override
                public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                    if (throwable instanceof UnresponsiveDomainException) {
                        final UnresponsiveDomainException unresponsiveDomainException = (UnresponsiveDomainException) throwable;
                        final Domain domain = unresponsiveDomainException.getDomain();
                        final String domainName = domain.getName();
                        if (!unresponsiveDomains.containsKey(domainName)) {
                            unresponsiveDomains.put(domainName, Calendar.getInstance().getTimeInMillis());
                        }

                        /* Mark the domain as unresponsive */
                        final Domain unresponsiveDomain = new Domain(domain, DomainStatus.UNRESPONSIVE);
                        getParent().tell(new UpdateDomainRequest(unresponsiveDomain), getSelf());

                        /* Stop the actor */
                        return SupervisorStrategy.stop();
                    }

                    if (throwable instanceof ExhaustedDomainException) {
                        final ExhaustedDomainException exhaustedDomainException = (ExhaustedDomainException) throwable;
                        final Domain domain = exhaustedDomainException.getDomain();
                        final String domainName = domain.getName();
                        if (!exhaustedDomains.containsKey(domainName)) {
                            exhaustedDomains.put(domainName, Calendar.getInstance().getTimeInMillis());
                        }

                        /* Mark the domain as exhausted */
                        Domain exhaustedDomain = new Domain(domain, DomainStatus.EXHAUSTED);
                        getParent().tell(new UpdateDomainRequest(exhaustedDomain), getSelf());

                        /* Stop the actor */
                        return SupervisorStrategy.stop();
                    }

                    return SupervisorStrategy.restart();
                }
            }
    );

    public DomainMasterActor(final ActorRef masterActor) {
        this.domainActors = new HashMap<>();

        this.unresponsiveDomains = new HashMap<>();
        this.exhaustedDomains = new HashMap<>();

        this.parent = masterActor;
    }

    @Override
    public void onReceive(final Object message) {
        if (message instanceof RefreshDomainMasterRequest) {
            getParent().tell(new ListCrawlableDomainsRequest(), getSelf());

        } else if (message instanceof ListCrawlableDomainsResponse) {
            processCrawlableDomains((ListCrawlableDomainsResponse) message);

        } else if (message instanceof NextLinkResponse) {
            routeNextLinkToDomain((NextLinkResponse) message);

        } else if (message instanceof DownloadUrlRequest) {
            getParent().tell(message, getSelf());

        } else if (message instanceof DownloadUrlResponse) {
            routeDownloadUrlToDomain((DownloadUrlResponse) message);

        } else if (message instanceof DomainStartedMessage) {
            startDomain((DomainStartedMessage) message);

        } else if (message instanceof DomainStoppedMessage) {
            stopDomain((DomainStoppedMessage) message);

        } else if (message instanceof ProcessingRequest) {
            getParent().tell(message, getSelf());
        } else if (message instanceof PersistenceRequest) {
            getParent().tell(message, getSelf());
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void processCrawlableDomains(final ListCrawlableDomainsResponse listCrawlableDomainsResponse) {
        /* Check domains processing limit */
        if (domainActors.size() >= CrawlerConstants.DOMAINS_CRAWL_MAX_COUNT) {
            LOG.info(String.format("The number of maximum domains for processing was reached. (MAX = %d)",
                    CrawlerConstants.DOMAINS_CRAWL_MAX_COUNT));
        }

        int slotsLeft = CrawlerConstants.DOMAINS_CRAWL_MAX_COUNT - domainActors.size();
        /* Start an actor for each domain, if not already started */
        for (final Domain domain : listCrawlableDomainsResponse.getCrawlableDomains()) {
            /* Check if the current domain is in the exceptions lists */
            if (unresponsiveDomains.containsKey(domain.getName())) {
                LOG.info("Unresponsive domain: " + domain.getName() + ". This domain will not be processed.");
                continue;
            }
            if (exhaustedDomains.containsKey(domain.getName())) {
                LOG.info("Exhausted domain: " + domain.getName() + ". This domain will not be processed.");
                continue;
            }

            /* Is this domain in processing? */
            if (!domainActors.containsKey(domain.getName()) && slotsLeft > 0) {
                startNewDomain(domain);
                slotsLeft--;
            }
        }

        /* Schedule a domains list refresh */
        getContext().system().scheduler().scheduleOnce(Duration.create(CrawlerConstants.DOMAINS_REFRESH_PERIOD, TimeUnit.MILLISECONDS),
                getSelf(), new RefreshDomainMasterRequest(), getContext().system().dispatcher(), getSelf());
    }

    /**
     * Creates an actor for the new domain and sends a crawl request
     */
    private void startNewDomain(final Domain domain) {
        final ActorRef domainActor = getContext().actorOf(Props.create(DomainActor.class, getSelf(), domain),
                CrawlerConstants.DOMAIN_ACTOR_PART_NAME + getActorName(domain.getName()));

        LOG.info("Domain " + domain.getName() + " starting actor " + domainActor);

        domainActor.tell(new CrawlDomainRequest(domain), getSelf());
    }

    /**
     * Route this message to the domain actor responsible with processing it
     */
    private void routeNextLinkToDomain(final NextLinkResponse nextLinkResponse) {
        final ActorRef domainActor = domainActors.get(nextLinkResponse.getNextLinkRequest().getDomain().getName());
        if (domainActor != null) {
            domainActor.tell(nextLinkResponse, getSelf());
        }
    }

    private void routeDownloadUrlToDomain(final DownloadUrlResponse downloadUrlResponse) {
        final ActorRef domainActor = domainActors.get(downloadUrlResponse.getDownloadUrlRequest().getDomain().getName());
        if (domainActor != null) {
            domainActor.tell(downloadUrlResponse, getSelf());
        }
    }

    private void startDomain(final DomainStartedMessage domainStarted) {
        final String domainName = domainStarted.getDomain().getName();
        domainActors.put(domainName, getSender());
    }

    private void stopDomain(final DomainStoppedMessage domainStopped) {
        final String domainName = domainStopped.getDomain().getName();
        domainActors.remove(domainName);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return supervisorStrategy;
    }

    public ActorRef getParent() {
        return parent;
    }
}