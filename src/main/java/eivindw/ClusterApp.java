package eivindw;

import akka.actor.ActorSystem;

public class ClusterApp {

   public static void main(String[] args) {
      if (args.length > 0) {
         System.setProperty("akka.remote.netty.port", args[0]);
      }

      final ActorSystem actorSystem = ActorSystem.create("ClusterExample");
   }
}
