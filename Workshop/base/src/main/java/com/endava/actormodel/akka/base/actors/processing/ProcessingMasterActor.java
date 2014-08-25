package com.endava.actormodel.akka.base.actors.processing;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.FromConfig;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.messages.persistence.PersistenceRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessContentRequest;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Processing master actor
 */
public class ProcessingMasterActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    // Define the processing routers
    private final ActorRef indentifyLinksRouter;
    private final ActorRef dataExtractorRouter;

    private ActorRef parent;

    public ProcessingMasterActor(ActorRef parent) {

        this.parent = parent;

        final SupervisorStrategy routersSupervisorStrategy = new OneForOneStrategy(-1, Duration.create(1, TimeUnit.MINUTES),
                new Function<Throwable, SupervisorStrategy.Directive>() {
                    @Override
                    public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                        if (throwable instanceof Exception) {
                            LOG.error("Exception in ProcessingMasterActor: type [" + throwable.getClass() + "], message [" +
                                    throwable.getMessage() + ". Will restart.");
                            return SupervisorStrategy.restart();
                        }

                        LOG.error("Exception in ProcessingMasterActor: type [" + throwable.getClass() + "], message [" +
                                throwable.getMessage() + ". Will stop.");
                        return SupervisorStrategy.stop();
                    }
                });

        this.indentifyLinksRouter = getContext().actorOf(Props.create(IdentifyLinksActor.class, getSelf()).withRouter(
                new FromConfig().withSupervisorStrategy(routersSupervisorStrategy)), "indentifyLinksRouter");
        this.dataExtractorRouter = getContext().actorOf(Props.create(DataExtractorActor.class, getSelf()).withRouter(
                new FromConfig().withSupervisorStrategy(routersSupervisorStrategy)), "dataExtractorRouter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Object message) {
        LOG.debug("Received message " + message.getClass());
        if (message instanceof ProcessContentRequest) {
            //identify the links
            indentifyLinksRouter.tell(message, getSender());
            //extract the data
            dataExtractorRouter.tell(message, getSender());
        } else if (message instanceof PersistenceRequest) {
            parent.tell(message, getSender());
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }
    }

}