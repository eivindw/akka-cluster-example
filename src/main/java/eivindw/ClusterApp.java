package eivindw;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.contrib.pattern.ClusterSingletonPropsFactory;
import akka.routing.RandomRouter;
import eivindw.actors.MasterActor;
import eivindw.actors.MasterProxy;
import eivindw.actors.WorkerActor;

public class ClusterApp {

   public static void main(String[] args) {
      if (args.length > 0) {
         System.setProperty("akka.remote.netty.port", args[0]);
      }

      final ActorSystem actorSystem = ActorSystem.create("ClusterExample");

      actorSystem.actorOf(new Props(new UntypedActorFactory() {
         @Override
         public Actor create() throws Exception {
            return new ClusterSingletonManager(
               "master",
               PoisonPill.getInstance(),
               new ClusterSingletonPropsFactory() {
                  @Override
                  public Props create(Object handOverData) {
                     return new Props(MasterActor.class);
                  }
               }
            );
         }
      }), "singleton");

      actorSystem.actorOf(new Props(WorkerActor.class).withRouter(new RandomRouter(5)));

      Cluster.get(actorSystem).registerOnMemberUp(new Runnable() {
         @Override
         public void run() {
            actorSystem.actorOf(new Props(MasterProxy.class));
         }
      });
   }
}
