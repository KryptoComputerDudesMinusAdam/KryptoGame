import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public String e;
    public int option;

    public Message(String e, int option){
        this.e = e;
        this.option = option;
    }

    public Message(String e){
        this.e = e;
        this.option = -1;
    }
}
