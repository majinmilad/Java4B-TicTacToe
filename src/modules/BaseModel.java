package modules;

import java.io.Serializable;

public class BaseModel implements Serializable {
    private int id;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
