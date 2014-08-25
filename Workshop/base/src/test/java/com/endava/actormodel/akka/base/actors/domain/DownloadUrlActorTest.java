package com.endava.actormodel.akka.base.actors.domain;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.endava.actormodel.akka.base.entities.Domain;
import com.endava.actormodel.akka.base.entities.Link;
import com.endava.actormodel.akka.base.enums.LinkStatus;
import com.endava.actormodel.akka.base.messages.domain.DownloadUrlRequest;
import com.endava.actormodel.akka.base.messages.persistence.UpdateLinkRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import static org.junit.Assert.*;

public class DownloadUrlActorTest {

    private static final FiniteDuration TIMEOUT = JavaTestKit.duration("5 seconds");
    
    private ActorSystem actorSystem;
    
    @Before
    public void setUp() throws Exception {
        actorSystem = ActorSystem.create("TestCrawlerActorSystem");
    }

    @After
    public void tearDown() throws Exception {
        actorSystem.shutdown();
        //allow enough time to shutdown
        Thread.sleep(100);
    }

    @Test
    public void testDownloadInvalidUrl() {
        new JavaTestKit(actorSystem) {
            {
                final ActorRef downloadUrlActor = actorSystem.actorOf(Props.create(DownloadUrlActor.class));
                Domain domain = new Domain("www.google.ro", 100000);
                Link link = new Link("www.google.ro", "http://www.google.ro/bad_url", null);
                DownloadUrlRequest request = new DownloadUrlRequest(domain, link);
                downloadUrlActor.tell(request, getRef());

                UpdateLinkRequest response = expectMsgClass(TIMEOUT, UpdateLinkRequest.class);
                
                assertEquals("Status should be FAILED", LinkStatus.FAILED, response.getLink().getStatus());
            }
        };
    }
}
