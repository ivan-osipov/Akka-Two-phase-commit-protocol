package utils;

import java.util.Observable;

public class ObservableState<T extends Enum> extends Observable {
    private T state;

    public ObservableState(T state) {
        this.state = state;
        setChanged();
        notifyObservers(state);
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
        setChanged();
        notifyObservers(state);
    }
}
