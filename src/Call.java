import java.util.Date;

public class Call {
    private User sender;
    private User receiver;
    private Group group;
    private Date startTime;
    private Date endTime;
    
    // Constructor para llamadas a usuarios individuales
    public Call(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.startTime = new Date();
    }
    
    // Constructor para llamadas a grupos
    public Call(User sender, Group group) {
        this.sender = sender;
        this.group = group;
        this.startTime = new Date();
    }
    
    // Método para finalizar la llamada y registrar la duración
    public void endCall() {
        this.endTime = new Date();
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    
}