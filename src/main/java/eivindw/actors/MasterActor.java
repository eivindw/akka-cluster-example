package eivindw.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import eivindw.messages.ConstantMessages;
import scala.concurrent.duration.Duration;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MasterActor extends AbstractActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

   private static final Random RANDOM = new Random();

   public MasterActor() {
      log.info("Starting master!");

      receive(ReceiveBuilder
            .matchEquals(MSG_WAKE_UP, msg -> {
               log.info("[Master] Scheduled wake-up!");
               mediator.tell(new DistributedPubSubMediator.Publish(TOPIC_WORKERS, MSG_WORK_AVAILABLE), self());
               scheduleWakeUp();
            })
            .matchEquals(MSG_GIVE_WORK, msg -> {
               if (RANDOM.nextBoolean()) { // obtain real work here
                  getContext().watch(sender());
                  sender().tell(MSG_WORK, self());
               }
            })
            .matchEquals(MSG_WORK_DONE, msg -> getContext().unwatch(sender()))
            .match(Terminated.class, msg -> log.info("Active worker crashed: " + msg.getActor()))
            .build()
      );

      scheduleWakeUp();
   }

   private void scheduleWakeUp() {
      context().system().scheduler().scheduleOnce(
         Duration.create(5, TimeUnit.SECONDS),
         self(),
         MSG_WAKE_UP,
         context().dispatcher(),
         null
      );
   }
}
