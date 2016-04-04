package actors;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import messages.Message;
import messages.commit_protocol.*;
import messages.system.ChangeCoordinatorProperties;
import messages.system.TransactionDestroy;
import messages.system.TransactionInit;
import utils.ObservableState;

import java.util.Collection;
import java.util.HashSet;

public class Coordinator extends StateActor<Coordinator.State> {
    public static final State DEFAULT_STATE = State.INIT;

    private Collection<ActorRef> participants = new HashSet<>();

    private int votedAmount = 0;
    private TransactionInit.Callback callback;

    public Coordinator() {
        super();
        setName("Coordinator");
        setState(DEFAULT_STATE);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof VoteCommit) {
            votedAmount++;
            if(votedAmount == participants.size()) {
                resetTimeout();
                setState(State.COMMIT);
                broadcast(new GlobalCommit());
                callback.onCommit();
            }
        } else if(o instanceof VoteAbort){
            votedAmount = 0;
            if(!State.ABORT.equals(getState())) {
                setState(State.ABORT);
                broadcast(new GlobalAbort());
            }
            callback.onAbort();
        } else if(o instanceof ReceiveTimeout) {
            resetTimeout();
            if(!State.ABORT.equals(getState())) {
                votedAmount = 0;
                setState(State.ABORT);
                broadcast(new GlobalAbort());
            }
            callback.onTimeout();
        } else if(o instanceof ChangeCoordinatorProperties) {
            ChangeCoordinatorProperties propMsg = (ChangeCoordinatorProperties) o;
            timeoutInSec = propMsg.getTimeout();
            setOnline(propMsg.isOnline());
        } else if(o instanceof TransactionInit) {
            setState(State.INIT);
            votedAmount = 0;
            TransactionInit initMsg = (TransactionInit) o;
            setTimeout();
            voteRequest(initMsg.getParticipants(), initMsg.getCallback());
        } else if(o instanceof TransactionDestroy) {
            resetTimeout();
            setState(State.ABORT);
            broadcast(new GlobalAbort());
            callback.onAbort();
        }
    }

    public void voteRequest(Collection<ActorRef> participants, TransactionInit.Callback callback) {
        this.participants = participants;
        this.callback = callback;
        broadcast(new VoteRequest());
        setState(State.WAIT);
        setTimeout();
    }

    public void broadcast(Message message) {
        for (ActorRef participant : participants) {
            participant.tell(message, getSelf());
        }
    }

    @Override
    ObservableState<State> createStateSet() {
        return new ObservableState<>(DEFAULT_STATE);
    }

    enum State {
        INIT,
        WAIT,
        ABORT,
        COMMIT
    }
}
