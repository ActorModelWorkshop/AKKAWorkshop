package com.endava.actormodel.akka.base.actors.processing;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.messages.persistence.PersistNewDomainsAndLinksRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessContentRequest;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import com.endava.actormodel.akka.base.tools.WebClient;
import com.endava.actormodel.akka.base.tools.WebContentTools;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor that will process the page content and identify the next links that can be crawled
 */
public class IdentifyLinksActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final ActorRef parent; // ProcessingMasterActor

    public IdentifyLinksActor(final ActorRef parent) {
        this.parent = parent;
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof ProcessContentRequest) {
            processContent((ProcessContentRequest) message);
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void processContent(final ProcessContentRequest processContentRequest) throws MalformedURLException {
        final List<Domain> pageDomains = new ArrayList<>();
        final List<Link> pageLinks = new ArrayList<>();

        final String sourceUrl = processContentRequest.getSourceLink().getUrl();
        final Document processedDocument = Jsoup.parse(processContentRequest.getContent(), sourceUrl);
        final Elements links = processedDocument.select("a[href]");
        for (final Element link : links) {
            final String normalizedLink = WebContentTools.normalizeURLLink(link.attr("abs:href"));

            if (isLinkValid(normalizedLink)) {
                final Optional<Domain> newDomain = createDomain(normalizedLink, processContentRequest.getSourceDomain());
                if (newDomain.isPresent()) {
                    pageDomains.add(newDomain.get());
                }

                pageLinks.add(createLink(normalizedLink, sourceUrl));
            }
        }

        persistDomainsAndLinks(pageDomains, pageLinks);
    }

    private boolean isLinkValid(final String foundLink) throws MalformedURLException {
        return !StringUtils.isBlank(foundLink) && WebClient.isValid(foundLink) && WebClient.isProtocolAccepted(foundLink);
    }

    private Link createLink(final String foundLink, final String sourceUrl) throws MalformedURLException {
        final URL foundLinkUrl = new URL(foundLink);
        final String linkDomain = foundLinkUrl.getHost();

        return new Link(linkDomain, foundLink, sourceUrl);
    }

    private Optional<Domain> createDomain(final String foundLink, final Domain sourceDomain) throws MalformedURLException {
        final URL foundLinkUrl = new URL(foundLink);
        final String linkDomain = foundLinkUrl.getHost();

        if (!linkDomain.equals(sourceDomain.getName())) {
            return Optional.of(new Domain(linkDomain, CrawlerConstants.DOMAIN_DEFAULT_COOLDOWN, DomainStatus.FOUND));
        }

        return Optional.absent();
    }

    /**
     * Sends the request to persist the new domains and links
     */
    private void persistDomainsAndLinks(final List<Domain> newDomains, final List<Link> newLinks) {
        getParent().tell(new PersistNewDomainsAndLinksRequest(newDomains, newLinks), getSelf());
    }

    public ActorRef getParent() {
        return parent;
    }
}
