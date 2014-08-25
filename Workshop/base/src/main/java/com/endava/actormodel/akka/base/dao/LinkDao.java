package com.endava.actormodel.akka.base.dao;

import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;

public interface LinkDao {

    /**
     * Get the next {@link com.endava.actormodel.akka.base.entities.Link} of a specified {@link com.endava.actormodel.akka.base.entities.Domain}
     * for crawling
     */
    public Link getNextForCrawling(final Domain domain);

    /**
     * Add a {@link com.endava.actormodel.akka.base.entities.Link}. If it is already added, do nothing.
     */
    public void add(final Link link);

    /**
     * Add or update a {@link com.endava.actormodel.akka.base.entities.Link}
     */
    public void addOrUpdate(final Link link);
}
