package com.endava.actormodel.akka.base.dao.impl;

import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.LinkStatus;
import com.endava.actormodel.akka.base.dao.LinkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository("linkDao")
public class InMemoryLinkDao implements LinkDao {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryLinkDao.class);

    private static final List<Link> LINKS = Collections.synchronizedList(new ArrayList<Link>());
    private static final Object linksLock = new Object();

    @Override
    public Link getNextForCrawling(final Domain domain) {
        /* Validation */
        if (null == domain) {
            LOG.error("Cannot scan a null Domain");
            return null;
        }

        synchronized (linksLock) {
            for (final Link link : LINKS) {
                if (link.getDomain().equals(domain.getName()) && link.getStatus().equals(LinkStatus.NOT_VISITED)) {
                    return link;
                }
            }
        }

        return null;
    }

    @Override
    public void add(final Link link) {
        /* Validation */
        if (null == link) {
            LOG.error("Cannot add or update a null Link");
            return;
        }

        synchronized (linksLock) {
            if (!LINKS.contains(link)) {
                LINKS.add(link);

                LOG.debug("Added link: " + link);
            }
        }
    }

    @Override
    public void addOrUpdate(final Link link) {
        /* Validation */
        if (null == link) {
            LOG.error("Cannot add or update a null Link");
            return;
        }

        LOG.debug("Add or update link: " + link);

        synchronized (linksLock) {
            /* Remove the old link */
            LINKS.remove(link);

            /* Add the new link */
            LINKS.add(link);
        }
    }
}
