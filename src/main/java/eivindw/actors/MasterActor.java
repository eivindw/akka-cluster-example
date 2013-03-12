package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import eivindw.messages.ConstantMessages;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MasterActor extends UntypedActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private static final Random RANDOM = new Random();

   private Set<ActorRef> workers;

   public MasterActor() {
      workers = new HashSet<>();
      scheduleWakeUp();
   }

   @Override
   public void onReceive(Object message) throws Exception {
      if(message.equals(MSG_WAKE_UP)) {
         log.info("Scheduled wake-up - worker count: " + workers.size());
         for (ActorRef worker : workers) {
            worker.tell(MSG_WORK_AVAILABLE, getSelf());
         }
         scheduleWakeUp();
      } else if(message.equals(MSG_REGISTER_WORKER)) {
         log.info("New worker: " + getSender());
         getContext().watch(getSender());
         workers.add(getSender());
      } else if(message instanceof Terminated) {
         Terminated terminated = (Terminated) message;
         log.info("Removing worker: " + terminated.getActor());
         workers.remove(terminated.getActor());
      } else if(message.equals(MSG_GIVE_WORK)) {
         if(RANDOM.nextBoolean()) {
            getSender().tell(MSG_WORK, getSelf());
         }
      } else if(message.equals(MSG_PING)) {
         getSender().tell(MSG_PONG, getSelf());
      } else {
         unhandled(message);
      }
   }

   private void scheduleWakeUp() {
      context().system().scheduler().scheduleOnce(
         Duration.create(2, TimeUnit.SECONDS),
         getSelf(),
         MSG_WAKE_UP,
         context().dispatcher()
      );
   }
}
