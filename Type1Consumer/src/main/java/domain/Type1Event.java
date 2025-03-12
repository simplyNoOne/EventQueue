package domain;
import javax.annotation.processing.Generated;
import java.io.Serializable;

public class Type1Event implements Serializable {
    private final String id;

    public Type1Event(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
