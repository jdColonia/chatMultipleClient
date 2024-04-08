import java.io.Serializable;

public class VoiceCall implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String receiver;
    private byte[] voiceData;

    public VoiceCall(String sender, String receiver, byte[] voiceData) {
        this.sender = sender;
        this.receiver = receiver;
        this.voiceData = voiceData;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public byte[] getVoiceData() {
        return voiceData;
    }
}
