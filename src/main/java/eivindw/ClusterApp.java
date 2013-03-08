package eivindw;

import akka.actor.*;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.contrib.pattern.ClusterSingletonPropsFactory;
import eivindw.actors.MasterActor;

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
               "MasterActor",
               PoisonPill.getInstance(),
               new ClusterSingletonPropsFactory() {
                  @Override
                  public Props create(Object handOverData) {
                     System.out.println("Creating MasterActor");
                     return new Props(MasterActor.class);
                  }
               }
            );
         }
      }));
   }
}
