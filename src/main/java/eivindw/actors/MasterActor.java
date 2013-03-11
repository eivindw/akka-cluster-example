package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import eivindw.messages.ConstantMessages;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MasterActor extends UntypedActor implements ConstantMessages {

   private Set<ActorRef> workers;

   public MasterActor() {
      workers = new HashSet<>();
      scheduleWakeUp();
   }

   @Override
   public void onReceive(Object message) throws Exception {
      System.out.println(getSelf() + " got " + message + " from " + getSender());

      if(message.equals(MSG_WAKE_UP)) {
         for (ActorRef worker : workers) {
            worker.tell(MSG_WORK_AVAILABLE, getSelf());
         }
         scheduleWakeUp();
      } else if(message.equals(MSG_REGISTER_WORKER)) {
         workers.add(getSender());
      } else if(message.equals(MSG_GIVE_WORK)) {
         // Give some work?
      } else {
         unhandled(message);
      }
   }

   private void scheduleWakeUp() {
      context().system().scheduler().scheduleOnce(
         Duration.create(5, TimeUnit.SECONDS),
         getSelf(),
         MSG_WAKE_UP,
         context().dispatcher()
      );
   }
}
