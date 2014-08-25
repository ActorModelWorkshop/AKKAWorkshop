package com.endava.actormodel.akka.downloader;

import akka.actor.ActorSystem;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page content downloader manager
 */
public class Downloader {
    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    public static void main(final String[] args) {
        LOG.info("Downloader starting ...");

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

        LOG.info("Downloader started and operational");
    }

    private static ActorSystem actorSystemInitialization() {
        return ActorSystem.create(CrawlerConstants.SYSTEM_DOWNLOADER, ConfigFactory.load("downloader.conf"));
    }
}
