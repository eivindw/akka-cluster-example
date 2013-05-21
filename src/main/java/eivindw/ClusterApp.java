package eivindw;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.contrib.pattern.ClusterSingletonPropsFactory;
import akka.routing.RandomRouter;
import eivindw.actors.MasterActor;
import eivindw.actors.WorkerActor;

import static eivindw.messages.ConstantMessages.TOPIC_WORKERS;

public class ClusterApp {

   public static void main(String[] args) {
      if (args.length > 0) {
         System.setProperty("akka.remote.netty.tcp.port", args[0]);
      }

      final ActorSystem actorSystem = ActorSystem.create("ClusterExample");

      actorSystem.actorOf(ClusterSingletonManager.defaultProps("master", PoisonPill.getInstance(), null,
         new ClusterSingletonPropsFactory() {
            @Override
            public Props create(Object handOverData) {
               return Props.create(MasterActor.class);
            }
         }
      ), "singleton");

      actorSystem.actorOf(Props.create(WorkerActor.class).withRouter(new RandomRouter(5)), TOPIC_WORKERS);
   }
}
