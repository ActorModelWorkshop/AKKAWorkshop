package com.endava.actormodel.akka.base.dao;

import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.enums.DomainStatus;

import java.util.List;

public interface DomainDao {

    /**
     * Find all {@link com.endava.actormodel.akka.base.entities.Domain}s
     */
    public List<Domain> findAll();

    /**
     * Find all {@link com.endava.actormodel.akka.base.entities.Domain}s that have the specified statuses
     */
    public List<Domain> findAll(final List<DomainStatus> domainStatuses);

    /**
     * Add a {@link com.endava.actormodel.akka.base.entities.Domain}. If it is already added, do nothing.
     */
    public void add(final Domain domain);

    /**
     * Add or update a {@link com.endava.actormodel.akka.base.entities.Domain}
     */
    public void addOrUpdate(final Domain domain);
}
