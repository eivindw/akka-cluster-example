package eivindw.actors;

import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import eivindw.messages.ConstantMessages;
import eivindw.messages.MasterChanged;

import java.util.concurrent.Callable;

public class WorkerActor extends UntypedActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private boolean working = false;

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
         if(working) {
            //log.info("Working... not interested!");
         } else {
            getSender().tell(MSG_GIVE_WORK, getSelf());
         }
      } else if(message instanceof MasterChanged) {
         MasterChanged masterChanged = (MasterChanged) message;
         System.out.println(name() + " New master: " + masterChanged.getMaster());
         masterChanged.getMaster().tell(MSG_REGISTER_WORKER, getSelf());
      } else if(message.equals(MSG_WORK)) {
         System.out.println(name() + " Got work!");
         working = true;
         Futures.future(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
               Thread.sleep(10000);
               working = false;
               return null;
            }
         }, getContext().dispatcher());
      } else {
         unhandled(message);
      }
   }

   private String name() {
      return "[Worker " + getSelf().path().name() + "]";
   }
}
