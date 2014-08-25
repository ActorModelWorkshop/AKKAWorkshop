package com.endava.actormodel.akka.base.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Base actor definition and useful methods
 */
public abstract class BaseActor extends UntypedActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private static final FiniteDuration ACTOR_FIND_TIMEOUT = Duration.create(10, TimeUnit.SECONDS);

    @Override
    public void postStop() throws Exception {
        LOG.warning("Actor " + getSelf().getClass() + " will stop.");

        super.postStop();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        LOG.error("Actor " + getSelf().getClass() + " will be restarted. The reason class [" + reason.getClass() + "] " +
                "message [" + reason.getMessage() + "].");
        reason.printStackTrace();

        super.preRestart(reason, message);
    }

    @Override
    public void preStart() throws Exception {
        LOG.info("Actor " + getSelf().getClass() + " will start.");

        super.preStart();
    }

    /**
     * Process the string and convert it to a valid actor name
     *
     * @param name The original name
     * @return The valid actor name
     */
    protected String getActorName(final String name) {
        return name.replace('.', '_').replace(':', '_').replace('/', '_');
    }
}