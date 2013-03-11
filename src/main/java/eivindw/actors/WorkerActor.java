package eivindw.actors;

import akka.actor.UntypedActor;
import eivindw.messages.ConstantMessages;
import eivindw.messages.MasterChanged;

public class WorkerActor extends UntypedActor implements ConstantMessages {

   @Override
   public void preStart() {
      getContext().system().eventStream().subscribe(getSelf(), MasterChanged.class);
   }

   @Override
   public void postStop() {
      getContext().system().eventStream().unsubscribe(getSelf());
   }

   @Override
   public void onReceive(Object message) throws Exception {
      if(message.equals(MSG_WORK_AVAILABLE)) {
         getSender().tell(MSG_GIVE_WORK, getSelf());
      } else if(message instanceof MasterChanged) {
         MasterChanged masterChanged = (MasterChanged) message;
         System.out.println(getSelf() + " new master: " + masterChanged.getMaster());
         masterChanged.getMaster().tell(MSG_REGISTER_WORKER, getSelf());
      }
   }
}
