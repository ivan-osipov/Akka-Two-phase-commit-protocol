package messages.system;

public class ChangeCoordinatorProperties {
    private long timeout;
    private boolean online;

    public ChangeCoordinatorProperties(long timeout, boolean online) {
        this.timeout = timeout;
        this.online = online;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean isOnline() {
        return online;
    }
}
