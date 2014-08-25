package com.endava.actormodel.akka.base.entities;

import com.endava.actormodel.akka.base.enums.DomainStatus;

import java.io.Serializable;

/**
 * A domain entity
 */
public class Domain implements Serializable {

    private final String name;
    private final DomainStatus domainStatus;
    private final long coolDownPeriod;

    public Domain(final String name, final long coolDownPeriod, final DomainStatus domainStatus) {
        this.name = name;
        this.coolDownPeriod = coolDownPeriod;
        this.domainStatus = domainStatus;
    }

    public Domain(final Domain sourceDomain, final DomainStatus domainStatus) {
        this(sourceDomain.getName(), sourceDomain.getCoolDownPeriod(), domainStatus);
    }

    public Domain(final String name, final long coolDownPeriod) {
        this(name, coolDownPeriod, DomainStatus.FOUND);
    }

    public String getName() {
        return name;
    }

    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }

    public DomainStatus getDomainStatus() {
        return domainStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Domain domain = (Domain) o;

        if (!name.equals(domain.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Domain{" +
                "name='" + name + '\'' +
                ", coolDownPeriod=" + coolDownPeriod +
                ", domainStatus=" + domainStatus +
                '}';
    }
}
