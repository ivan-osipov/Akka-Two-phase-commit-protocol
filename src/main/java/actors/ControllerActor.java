package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import messages.system.*;

import java.util.HashMap;
import java.util.Map;


public class ControllerActor extends UntypedActor {

    private ActorRef coordinator;
    private Map<String, ActorRef> participants = new HashMap<>();

    public ControllerActor() {
        coordinator = getContext().system().actorOf(Props.create(Coordinator.class));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof ChangeCoordinatorProperties) {
            coordinator.tell(o, getSelf());
        } else if (o instanceof CreateParticipant) {
            CreateParticipant createParticipant = (CreateParticipant) o ;
            ActorRef participant = getContext().system()
                    .actorOf(Props.create(
                            Participant.class,
                            createParticipant.getName(),
                            createParticipant.getTimeout(),
                            coordinator));
            participants.put(createParticipant.getName(), participant);
        } else if (o instanceof ChangeParticipantProperties) {
            ChangeParticipantProperties participantProperties = (ChangeParticipantProperties) o;
            participants.get(participantProperties.getName())
                    .tell(participantProperties, getSelf());
        } else if (o instanceof TransactionInit) {
            TransactionInit transactionInit = (TransactionInit) o;
            coordinator.tell(new TransactionInit(participants.values(), transactionInit.getCallback()), getSelf());
            for (ActorRef actorRef : participants.values()) {
                actorRef.tell(o, getSelf());
            }
        } else if(o instanceof TransactionDestroy) {
            coordinator.tell(o, getSelf());
        }
    }
}
