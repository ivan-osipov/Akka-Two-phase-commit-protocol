package messages.system;

public class ChangeParticipantProperties {
    private String name;
    private Long timeout;
    private boolean online;
    private Long delay;

    public ChangeParticipantProperties(String name, Long timeout, boolean online, Long delay) {
        this.name = name;
        this.timeout = timeout;
        this.online = online;
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public Long getTimeout() {
        return timeout;
    }

    public boolean isOnline() {
        return online;
    }

    public Long getDelay() {
        return delay;
    }
}
