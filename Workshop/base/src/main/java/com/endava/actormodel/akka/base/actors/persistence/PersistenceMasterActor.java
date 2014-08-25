package com.endava.actormodel.akka.base.actors.persistence;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.FromConfig;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.config.SpringContext;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Persistence master actor
 */
public class PersistenceMasterActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final ActorRef persistenceRouter;

    public PersistenceMasterActor() {
        final SupervisorStrategy routersSupervisorStrategy = new OneForOneStrategy(200, Duration.create(1, TimeUnit.MINUTES),
                new Function<Throwable, SupervisorStrategy.Directive>() {
                    @Override
                    public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                        if (throwable instanceof Exception) {
                            LOG.error("Exception in PersistenceMasterActor routers: type [" + throwable.getClass() +
                                    "], message [" + throwable.getMessage() + ". Will restart.");
                            return SupervisorStrategy.restart();
                        }

                        LOG.error("Exception in PersistenceMasterActor routers: type [" + throwable.getClass() +
                                "], message [" + throwable.getMessage() + ". Will stop.");
                        return SupervisorStrategy.stop();
                    }
                });

        this.persistenceRouter = getContext().actorOf(Props.create(PersistenceActor.class, SpringContext.getBean("domainDao"),
                        SpringContext.getBean("linkDao")).withRouter(new FromConfig().withSupervisorStrategy(routersSupervisorStrategy)),
                "persistenceRouter");
    }

    @Override
    public void onReceive(final Object message) {
        if (message instanceof PersistenceRequest) {
            persistenceRouter.tell(message, getSender());
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }
}