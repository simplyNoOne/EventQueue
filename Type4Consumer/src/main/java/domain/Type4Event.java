package domain;
import java.io.Serializable;

public class Type4Event implements Serializable {
    private final String id;

    public Type4Event(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
