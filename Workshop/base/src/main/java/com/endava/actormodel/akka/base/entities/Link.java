package com.endava.actormodel.akka.base.entities;

import com.endava.actormodel.akka.base.enums.LinkStatus;

import java.io.Serializable;

public class Link implements Serializable {

    private final String domain;
    private final String url;
    private final LinkStatus status;

    private final String sourceLink;

    public Link(final String domain, final String url, final String sourceLink, final LinkStatus status) {
        this.domain = domain;
        this.sourceLink = sourceLink;
        this.url = url;
        this.status = status;
    }

    public Link(final String domain, final String url, final String sourceLink) {
        this(domain, url, sourceLink, LinkStatus.NOT_VISITED);
    }

    public Link(final Link sourceLink, final LinkStatus status) {
        this(sourceLink.getDomain(), sourceLink.getUrl(), sourceLink.getSourceLink(), status);
    }

    public String getDomain() {
        return domain;
    }

    public String getUrl() {
        return url;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (url != null ? !url.equals(link.url) : link.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Link{" +
                "domain='" + domain + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status +
                ", sourceLink='" + sourceLink + '\'' +
                '}';
    }

}
