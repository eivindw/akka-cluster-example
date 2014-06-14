package eivindw.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import eivindw.messages.ConstantMessages;

public class WorkerActor extends AbstractActor implements ConstantMessages {

   private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

   private final ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();

   private boolean working = false;

   public WorkerActor() {
      receive(ReceiveBuilder
            .matchEquals(MSG_WORK_AVAILABLE, msg -> {
               if (!working) {
                  sender().tell(MSG_GIVE_WORK, self());
               }
            })
            .matchEquals(MSG_WORK, msg -> {
               final ActorRef master = sender();
               log.info("Got work!");
               working = true;
               Futures.future(() -> {
                  Thread.sleep(10000); // real work code goes here
                  working = false;
                  master.tell(MSG_WORK_DONE, self());
                  return null;
               }, getContext().dispatcher());
            })
            .match(DistributedPubSubMediator.SubscribeAck.class, msg ->
               log.info("Subscribed to 'workers'!"))
            .build()
      );
   }

   @Override
   public void preStart() {
      mediator.tell(new DistributedPubSubMediator.Subscribe(TOPIC_WORKERS, self()), self());
   }

   @Override
   public void postStop() {
      mediator.tell(new DistributedPubSubMediator.Unsubscribe(TOPIC_WORKERS, self()), self());
   }
}
