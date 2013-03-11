package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import eivindw.messages.MasterChanged;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.cluster.ClusterEvent.CurrentClusterState;
import static akka.cluster.ClusterEvent.LeaderChanged;

public class MasterProxy extends UntypedActor {

   private final Cluster cluster = Cluster.get(getContext().system());

   @Override
   public void preStart() {
      cluster.subscribe(getSelf(), LeaderChanged.class);
   }

   @Override
   public void postStop() {
      cluster.unsubscribe(getSelf());
   }

   @Override
   public void onReceive(Object message) throws Exception {
      System.out.println(getSelf() + " got " + message + " from " + getSender());

      if(message instanceof CurrentClusterState) {
         CurrentClusterState state = (CurrentClusterState) message;
         publishNewMaster(state.getLeader(), state);
      } else if(message instanceof LeaderChanged) {
         LeaderChanged leaderChanged = (LeaderChanged) message;
         publishNewMaster(leaderChanged.getLeader(), leaderChanged);
      } else {
         unhandled(message);
      }
   }

   private void publishNewMaster(Address leader, Object message) {
      final ActorRef master = getContext().actorFor(leader + "/user/singleton/master");

      if(master.isTerminated()) {
         System.out.println("Master terminated - retry in 1 second");
         getContext().system().scheduler().scheduleOnce(
            Duration.create(1, TimeUnit.SECONDS),
            getSelf(),
            message,
            getContext().dispatcher()
         );
      } else {
         getContext().system().eventStream().publish(new MasterChanged(master));
      }
   }
}
