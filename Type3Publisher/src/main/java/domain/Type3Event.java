package domain;
import java.io.Serializable;

public class Type3Event implements Serializable {
    private final String id;

    public Type3Event(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
