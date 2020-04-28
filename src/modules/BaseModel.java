package modules;

import java.io.Serializable;

public class BaseModel implements Serializable {
    private String id;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
