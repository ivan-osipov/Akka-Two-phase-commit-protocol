package actors;

import akka.actor.UntypedActor;
import scala.concurrent.duration.Duration;
import utils.Constants;
import utils.ObservableState;

import java.util.concurrent.TimeUnit;

public abstract class StateActor<T extends Enum> extends UntypedActor {
    public static final String DEFAULT_NAME = "unnamed";
    protected Long timeoutInSec = 10L;
    protected boolean online = true;
    protected Long delay = Constants.DEFAULT_DELAY;

    protected String name = DEFAULT_NAME;
    private ObservableState<T> state;

    protected StateActor() {
        this.state = createStateSet();
        this.state.addObserver((o, arg) ->
                System.out.println("Actor: " + getName() + " set state: " + ((T)arg).name()));
    }

    protected void setTimeout() {
        context().setReceiveTimeout(Duration.create(timeoutInSec, TimeUnit.SECONDS));
    }
    protected void resetTimeout() {
        context().setReceiveTimeout(Duration.Undefined());
    }

    public StateActor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setState(T state) {
        this.state.setState(state);
    }

    protected T getState() {
        return this.state.getState();
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    abstract ObservableState<T> createStateSet();
}
