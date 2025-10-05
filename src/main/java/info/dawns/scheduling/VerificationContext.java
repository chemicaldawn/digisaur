package info.dawns.scheduling;

public class VerificationContext {

    public long messageId;
    public String verificationType;

    public VerificationContext(long messageId, String verificationType) {
        this.messageId = messageId;
        this.verificationType = verificationType;
    }
}
