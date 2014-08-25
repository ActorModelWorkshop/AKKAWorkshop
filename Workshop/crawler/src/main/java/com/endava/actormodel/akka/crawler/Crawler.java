package com.endava.actormodel.akka.crawler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.MasterActor;
import com.endava.actormodel.akka.base.config.SpringContext;
import com.endava.actormodel.akka.base.dao.DomainDao;
import com.endava.actormodel.akka.base.dao.LinkDao;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.DomainStatus;
import com.endava.actormodel.akka.base.messages.StartMasterRequest;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crawler manager
 */
public class Crawler {
    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

    public static void main(final String[] args) {
        LOG.info("Crawler starting ...");

        LOG.info("Configuration initialization ...");
        SpringContext.initialize();
        LOG.info("Configuration done");

        LOG.info("Seed data creation ...");
        createSeedData();
        LOG.info("Seed data added");

        LOG.info("Actor system initialization");
        final ActorSystem actorSystem = actorSystemInitialization();
        LOG.info("Actor system up and running");

        LOG.info("Adding graceful shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                actorSystem.shutdown();
                LOG.info("Actor system stopped");
            }
        });

        LOG.info("Crawler started and operational");
    }

    private static void createSeedData() {
        final DomainDao domainDao = SpringContext.getBean("domainDao");
        final LinkDao linkDao = SpringContext.getBean("linkDao");

        final Domain wikipediaDomain = new Domain("ro.wikipedia.org", CrawlerConstants.DOMAIN_DEFAULT_COOLDOWN, DomainStatus.FOUND);
        domainDao.addOrUpdate(wikipediaDomain);

        final Link wikipediaStartLink = new Link(wikipediaDomain.getName(), "http://ro.wikipedia.org/wiki/Pagina_principal%C4%83", null);
        linkDao.addOrUpdate(wikipediaStartLink);
    }

    private static ActorSystem actorSystemInitialization() {
        final ActorSystem actorSystem = ActorSystem.create(CrawlerConstants.SYSTEM_CRAWLER, ConfigFactory.load("crawler.conf"));

        final ActorRef masterActor = actorSystem.actorOf(Props.create(MasterActor.class), CrawlerConstants.MASTER_ACTOR_NAME);
        masterActor.tell(new StartMasterRequest(), ActorRef.noSender());

        return actorSystem;
    }
}
