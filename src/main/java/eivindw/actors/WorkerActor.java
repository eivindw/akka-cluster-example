package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import eivindw.messages.ConstantMessages;

import java.util.concurrent.Callable;

public class WorkerActor extends UntypedActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

   private boolean working = false;

   @Override
   public void preStart() {
      mediator.tell(new DistributedPubSubMediator.Subscribe(TOPIC_WORKERS, getSelf()), getSelf());
   }

   @Override
   public void postStop() {
      mediator.tell(new DistributedPubSubMediator.Unsubscribe(TOPIC_WORKERS, getSelf()), getSelf());
   }

   @Override
   public void onReceive(Object message) throws Exception {
      if(message.equals(MSG_WORK_AVAILABLE)) {
         if(!working) {
            getSender().tell(MSG_GIVE_WORK, getSelf());
         }
      } else if(message.equals(MSG_WORK)) {
         final ActorRef master = getSender();
         log.info("Got work!");
         working = true;
         Futures.future(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
               Thread.sleep(10000); // real work code goes here
               working = false;
               master.tell(MSG_WORK_DONE, getSelf());
               return null;
            }
         }, getContext().dispatcher());
      } else if(message instanceof DistributedPubSubMediator.SubscribeAck) {
         log.info("Subscribed to 'workers'!");
      } else {
         unhandled(message);
      }
   }
}
