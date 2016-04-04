package actors;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import messages.commit_protocol.*;
import messages.system.ChangeParticipantProperties;
import messages.system.TransactionInit;
import utils.ObservableState;

public class Participant extends StateActor<Participant.State> {
    public static final State DEFAULT_STATE = State.INIT;

    private String reason = "Undefined";

    private ActorRef coordinator;

    public Participant(String name, Long timeout, ActorRef coordinator) {
        super();
        this.name = name;
        this.timeoutInSec = timeout;
        this.coordinator = coordinator;
        setState(DEFAULT_STATE);

    }

    @Override
    public void onReceive(Object o) throws Exception {
        resetTimeout();
        if(o instanceof VoteRequest) {
            Thread.sleep(delay * 1000);
            if(online) {
                getSender().tell(new VoteCommit(), getSelf());
                setState(State.READY);
                setTimeout();
            } else {
                getSender().tell(new VoteAbort(getName(), reason), getSelf());
                setState(State.ABORT);
            }
        } else if (o instanceof GlobalCommit) {
            Thread.sleep(delay * 1000);
            setState(State.COMMIT);
        } else if(o instanceof GlobalAbort) {
            Thread.sleep(delay * 1000);
            setState(State.ABORT);
        }else if(o instanceof ReceiveTimeout) {
            setState(State.ABORT);
            coordinator.tell(new VoteAbort(getName(), "Timeout"), getSelf());
        } else if (o instanceof ChangeParticipantProperties) {
            ChangeParticipantProperties msg = (ChangeParticipantProperties) o;
            timeoutInSec = msg.getTimeout();
            setOnline(msg.isOnline());
            setDelay(msg.getDelay());
        } else if (o instanceof TransactionInit) {
            setState(State.INIT);
            if(online) {
                setTimeout();
            }
        }
    }

    @Override
    ObservableState<State> createStateSet() {
        return new ObservableState<>(DEFAULT_STATE);
    }

    enum State {
        INIT,
        READY,
        ABORT,
        COMMIT
    }

}
