package eivindw.actors;

import akka.actor.UntypedActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class MasterActor extends UntypedActor {

   public MasterActor() {
      System.out.println("MasterActor starting scheduler");
      scheduleWakeUp();
   }

   @Override
   public void onReceive(Object message) throws Exception {
      System.out.println(getSelf() + " got message: " + message);

      scheduleWakeUp();
   }

   private void scheduleWakeUp() {
      context().system().scheduler().scheduleOnce(
         Duration.create(5, TimeUnit.SECONDS),
         getSelf(),
         "WakeUp!",
         context().dispatcher()
      );
   }
}
