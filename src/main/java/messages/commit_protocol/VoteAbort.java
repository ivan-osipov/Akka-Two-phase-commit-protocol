package messages.commit_protocol;

import messages.Message;

public class VoteAbort implements Message {
    private String name;
    private String reason;

    public VoteAbort(String name, String reason) {
        this.name = name;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
