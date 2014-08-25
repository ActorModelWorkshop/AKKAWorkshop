package com.endava.actormodel.akka.base.dao.impl;

import com.endava.actormodel.akka.base.dao.DomainDao;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository("domainDao")
public class InMemoryDomainDao implements DomainDao {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDomainDao.class);

    public static final List<Domain> DOMAINS = Collections.synchronizedList(new ArrayList<Domain>());
    public static final Object domainsLock = new Object();

    @Override
    public List<Domain> findAll() {
        return Collections.unmodifiableList(DOMAINS);
    }

    @Override
    public List<Domain> findAll(final List<DomainStatus> domainStatuses) {
        final List<Domain> filteredDomains = new ArrayList<>();
        synchronized (domainsLock) {
            for (final Domain domain : DOMAINS) {
                if (domainStatuses.contains(domain.getDomainStatus())) {
                    filteredDomains.add(domain);
                }
            }
        }
        return Collections.unmodifiableList(filteredDomains);
    }

    @Override
    public void add(final Domain domain) {
        /* Validation */
        if (null == domain) {
            LOG.error("Cannot add or update a null Domain");
            return;
        }

        synchronized (domainsLock) {
            if (!DOMAINS.contains(domain)) {
                DOMAINS.add(domain);

                LOG.debug("Added domain: " + domain);
            }
        }
    }

    @Override
    public void addOrUpdate(final Domain domain) {
        /* Validation */
        if (null == domain) {
            LOG.error("Cannot add or update a null Domain");
            return;
        }

        LOG.debug("Add or update domain: " + domain);

        synchronized (domainsLock) {
            /* Remove the old domain */
            DOMAINS.remove(domain);

            /* Add a new domain */
            DOMAINS.add(domain);
        }
    }
}
