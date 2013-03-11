package eivindw.actors;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.dispatch.OnComplete;
import akka.util.Timeout;
import eivindw.messages.ConstantMessages;
import eivindw.messages.MasterChanged;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.cluster.ClusterEvent.CurrentClusterState;
import static akka.cluster.ClusterEvent.LeaderChanged;
import static akka.pattern.Patterns.ask;

public class MasterListener extends UntypedActor implements ConstantMessages {

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

   private void publishNewMaster(final Address leader, final Object message) {
      System.out.println("New leader address: " + leader);
      final ActorRef master = getContext().actorFor(leader + "/user/singleton/master");

      Future<Object> ping = ask(master, MSG_PING, new Timeout(Duration.create(1, TimeUnit.SECONDS)));

      ping.onComplete(new OnComplete<Object>() {
         @Override
         public void onComplete(Throwable failure, Object pong) throws Throwable {
            if(failure != null) {
               System.out.println("No reply from master - retry in 1 second");
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
      }, getContext().dispatcher());
   }
}
