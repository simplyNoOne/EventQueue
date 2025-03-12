package domain;
import java.io.Serializable;

public class Type2Event implements Serializable {
    private final String id;

    public Type2Event(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
