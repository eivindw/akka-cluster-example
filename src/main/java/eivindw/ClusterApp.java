package eivindw;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.routing.RandomPool;
import eivindw.actors.MasterActor;
import eivindw.actors.WorkerActor;

import static eivindw.messages.ConstantMessages.TOPIC_WORKERS;

public class ClusterApp {

   public static void main(String[] args) {
      if (args.length > 0) {
         System.setProperty("akka.remote.netty.tcp.port", args[0]);
      }

      final ActorSystem actorSystem = ActorSystem.create("ClusterExample");

      actorSystem.actorOf(ClusterSingletonManager.defaultProps(
         Props.create(MasterActor.class),
         "master",
         PoisonPill.getInstance(),
         null
      ), "singleton");

      actorSystem.actorOf(
         new RandomPool(5).props(Props.create(WorkerActor.class)),
         TOPIC_WORKERS
      );
   }
}
