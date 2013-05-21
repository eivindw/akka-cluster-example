package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import eivindw.messages.ConstantMessages;
import scala.concurrent.duration.Duration;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MasterActor extends UntypedActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

   private static final Random RANDOM = new Random();

   public MasterActor() {
      log.info("Starting master!");
      scheduleWakeUp();
   }

   @Override
   public void onReceive(Object message) throws Exception {
      if(message.equals(MSG_WAKE_UP)) {
         log.info("[Master] Scheduled wake-up!");

         mediator.tell(new DistributedPubSubMediator.Publish(TOPIC_WORKERS, MSG_WORK_AVAILABLE), getSelf());

         scheduleWakeUp();
      } else if(message instanceof Terminated) {
         Terminated terminated = (Terminated) message;
         log.info("Active worker crashed: " + terminated.getActor());
      } else if(message.equals(MSG_GIVE_WORK)) {
         if(RANDOM.nextBoolean()) {
            getContext().watch(getSender());
            getSender().tell(MSG_WORK, getSelf());
         }
      } else if(message.equals(MSG_WORK_DONE)) {
         getContext().unwatch(getSender());
      } else {
         unhandled(message);
      }
   }

   private void scheduleWakeUp() {
      context().system().scheduler().scheduleOnce(
         Duration.create(5, TimeUnit.SECONDS),
         getSelf(),
         MSG_WAKE_UP,
         context().dispatcher(),
         null
      );
   }
}
