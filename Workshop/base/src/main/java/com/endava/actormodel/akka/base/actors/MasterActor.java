package com.endava.actormodel.akka.base.actors;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.FromConfig;
import com.endava.actormodel.akka.base.CrawlerConstants;
import com.endava.actormodel.akka.base.actors.domain.DomainMasterActor;
import com.endava.actormodel.akka.base.actors.domain.DownloadUrlActor;
import com.endava.actormodel.akka.base.actors.persistence.PersistenceMasterActor;
import com.endava.actormodel.akka.base.actors.processing.ProcessingMasterActor;
import com.endava.actormodel.akka.base.messages.StartMasterRequest;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlRequest;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlResponse;
import com.endava.actormodel.akka.base.messages.domain.RefreshDomainMasterRequest;
import com.endava.actormodel.akka.base.messages.persistence.ListCrawlableDomainsResponse;
import com.endava.actormodel.akka.base.messages.persistence.NextLinkResponse;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessingRequest;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Crawler master (root) actor
 */
public class MasterActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private ActorRef domainMaster;
    private ActorRef processingMaster;
    private ActorRef persistenceMaster;

    private ActorRef downloadUrlsRouter;

    private final SupervisorStrategy supervisorStrategy = new OneForOneStrategy(-1, Duration.create(1, TimeUnit.MINUTES),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                @Override
                public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                    if (throwable instanceof Exception) {
                        LOG.error("Exception in MasterActor : type [" + throwable.getClass() + "], message [" +
                                throwable.getMessage() + ". Will restart.");

                        return SupervisorStrategy.restart();
                    }

                    LOG.error("Exception in MasterActor : type [" + throwable.getClass() + "], message [" +
                            throwable.getMessage() + ". Will stop.");

                    return SupervisorStrategy.stop();
                }
            });

    private final SupervisorStrategy downloadUrlsRouterStrategy = new OneForOneStrategy(-1, Duration.create(1, TimeUnit.MINUTES),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                @Override
                public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                    LOG.error("Exception in MasterActor routersSupervisorStrategy: type [" + throwable.getClass() + "], " +
                            "message [" + throwable.getMessage() + "]. DownloadUrlActor will restart.");

                    return SupervisorStrategy.restart();
                }
            }
    );

    @Override
    public void onReceive(final Object message) {
        if (message instanceof StartMasterRequest) {
            startMaster((StartMasterRequest) message);
        } else if (message instanceof DownloadUrlRequest) {
//            if (downloadUrlsRouter.isTerminated()) {
//                createDownloadRouter();
//            }
            downloadUrlsRouter.tell(message, getSelf());
        } else if (message instanceof DownloadUrlResponse) {
            domainMaster.tell(message, getSelf());
        } else if (message instanceof ProcessingRequest) {
            processingMaster.tell(message, getSelf());
        } else if (message instanceof PersistenceRequest) {
            persistenceMaster.tell(message, getSelf());
        } else if (message instanceof ListCrawlableDomainsResponse) {
            domainMaster.tell(message, getSelf());
        } else if (message instanceof NextLinkResponse) {
            domainMaster.tell(message, getSelf());
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

    private void startMaster(final StartMasterRequest startMasterRequest) {
        /* Start the domain master actor */
        domainMaster = getContext().actorOf(Props.create(DomainMasterActor.class, getSelf()), CrawlerConstants.DOMAIN_MASTER_ACTOR_NAME);
        domainMaster.tell(new RefreshDomainMasterRequest(), getSelf());
        LOG.debug("Started Domains master");

        /* Start the processing actor */
        processingMaster = getContext().actorOf(Props.create(ProcessingMasterActor.class, getSelf()), CrawlerConstants.PROCESSING_MASTER_ACTOR_NAME);
        LOG.debug("Started Processing master");

        /* Start the persistence actor */
        persistenceMaster = getContext().actorOf(Props.create(PersistenceMasterActor.class), CrawlerConstants.PERSISTENCE_MASTER_ACTOR_NAME);
        LOG.debug("Started Persistence master");

        createDownloadRouter();
    }

    private void createDownloadRouter() {
        /* Start the download URL router */
        downloadUrlsRouter = getContext().actorOf(Props.create(DownloadUrlActor.class).withRouter(
                new FromConfig().withSupervisorStrategy(downloadUrlsRouterStrategy)), "downloadUrlRouter");

        LOG.debug("Started Download URLs router");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return supervisorStrategy;
    }
}