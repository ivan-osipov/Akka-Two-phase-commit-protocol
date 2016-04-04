package messages.system;

import akka.actor.ActorRef;

import java.util.Collection;

public class TransactionInit {
    private Collection<ActorRef> participants;
    private Callback callback;

    public TransactionInit(Collection<ActorRef> participants, Callback callback) {
        this.participants = participants;
        this.callback = callback;
    }

    public Collection<ActorRef> getParticipants() {
        return participants;
    }

    public Callback getCallback() {
        return callback;
    }

    public interface Callback {
        void onCommit();
        void onAbort();
        void onTimeout();
    }
}
