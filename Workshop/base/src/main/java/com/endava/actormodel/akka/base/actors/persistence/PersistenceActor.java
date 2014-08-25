package com.endava.actormodel.akka.base.actors.persistence;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.dao.DomainDao;
import com.endava.actormodel.akka.base.dao.LinkDao;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import com.endava.actormodel.akka.base.messages.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Actor for managing the persistence requests
 */
public class PersistenceActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final DomainDao domainDao;
    private final LinkDao linkDao;

    public PersistenceActor(final DomainDao domainDao, final LinkDao linkDao) {
        this.domainDao = domainDao;
        this.linkDao = linkDao;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ListDomainsRequest) {
            processListDomainsRequest((ListDomainsRequest) message);
        } else if (message instanceof ListCrawlableDomainsRequest) {
            processListCrawlabeDomainsRequest((ListCrawlableDomainsRequest) message);
        } else if (message instanceof NextLinkRequest) {
            processNextLinkRequest((NextLinkRequest) message);
        } else if (message instanceof UpdateLinkRequest) {
            processUpdateLinkRequest((UpdateLinkRequest) message);
        } else if (message instanceof UpdateDomainRequest) {
            processUpdateDomainRequest((UpdateDomainRequest) message);
        } else if (message instanceof PersistNewDomainsAndLinksRequest) {
            processPersistNewDomainsAndLinksRequest((PersistNewDomainsAndLinksRequest) message);
        } else if (message instanceof PersistContentRequest) {
            processPersistContentRequest((PersistContentRequest) message);
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void processListDomainsRequest(final ListDomainsRequest listDomainsRequest) {
        final List<Domain> domains = domainDao.findAll();

        LOG.debug(String.format("[ListDomainsRequest] Returning %d domains", domains.size()));

        final ListDomainsResponse response = new ListDomainsResponse(listDomainsRequest, domains);
        getSender().tell(response, getSelf());
    }

    private void processListCrawlabeDomainsRequest(final ListCrawlableDomainsRequest request) {
        final List<DomainStatus> crawlableStatuses = new ArrayList<>();
        crawlableStatuses.add(DomainStatus.STARTED);
        crawlableStatuses.add(DomainStatus.FOUND);

        final List<Domain> domains = domainDao.findAll(crawlableStatuses);

        LOG.debug(String.format("[ListCrawlableDomainsRequest] Returning %d domains", domains.size()));

        final ListCrawlableDomainsResponse response = new ListCrawlableDomainsResponse(request, domains);
        getSender().tell(response, getSelf());
    }

    private void processNextLinkRequest(final NextLinkRequest request) {
        final Link link = linkDao.getNextForCrawling(request.getDomain());
        final NextLinkResponse response = new NextLinkResponse(request, link);

        LOG.debug(String.format("[NextLinkRequest] Next link to crawl: %s", null == link ? "NONE" : link.getUrl()));

        getSender().tell(response, getSelf());
    }

    private void processUpdateLinkRequest(final UpdateLinkRequest updateLinkRequest) {
        LOG.debug(String.format("[UpdateLinkRequest] Add or update link: %s", updateLinkRequest.getLink()));

        linkDao.addOrUpdate(updateLinkRequest.getLink());
    }

    private void processUpdateDomainRequest(final UpdateDomainRequest updateDomainRequest) {
        LOG.debug(String.format("[UpdateDomainRequest] Add or update domain: %s", updateDomainRequest.getDomain()));

        domainDao.addOrUpdate(updateDomainRequest.getDomain());
    }

    private void processPersistNewDomainsAndLinksRequest(final PersistNewDomainsAndLinksRequest persistDomainRequest) {
        LOG.info(String.format("[PersistNewDomainsAndLinksRequest] Will try to persist the %d domains and %d links",
                persistDomainRequest.getNewDomains().size(), persistDomainRequest.getNewLinks().size()));

        for (final Domain newDomain : persistDomainRequest.getNewDomains()) {
            domainDao.add(newDomain);
        }

        for (final Link newLink : persistDomainRequest.getNewLinks()) {
            linkDao.add(newLink);
        }
    }

    private void processPersistContentRequest(final PersistContentRequest persistContentRequest) {
        LOG.debug(String.format("[PersistContentRequest] Persist content of size: %d",
                persistContentRequest.getCleanedContent().length()));
    }
}
