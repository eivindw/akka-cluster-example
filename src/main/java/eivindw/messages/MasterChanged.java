package eivindw.messages;

import akka.actor.ActorRef;

public class MasterChanged {

   private final ActorRef master;

   public MasterChanged(ActorRef master) {
      this.master = master;
   }

   public ActorRef getMaster() {
      return master;
   }
}
