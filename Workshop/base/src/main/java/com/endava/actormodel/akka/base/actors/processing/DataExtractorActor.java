package com.endava.actormodel.akka.base.actors.processing;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.endava.actormodel.akka.base.actors.BaseActor;
import com.endava.actormodel.akka.base.messages.persistence.PersistContentRequest;
import com.endava.actormodel.akka.base.messages.processing.ProcessContentRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Actor that will extract the data and send it to the Persistence Master.
 */
public class DataExtractorActor extends BaseActor {
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private ActorRef parent;

    public DataExtractorActor(ActorRef parent) {
        this.parent = parent;
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof ProcessContentRequest) {
            LOG.debug("Received content to extract");

            final ProcessContentRequest processContentRequest = (ProcessContentRequest) message;
            String content = processContentRequest.getContent();

            Document document = Jsoup.parse(content);
            if (document == null || document.body() == null) {
                return;
            }
            final String strippedText = document.body().text();

            getParent().tell(new PersistContentRequest(processContentRequest.getSourceLink(), strippedText), getSelf());
        } else {
            LOG.error("Unknown message: " + message);
            unhandled(message);
        }

    }

    public ActorRef getParent() {
        return parent;
    }
}
