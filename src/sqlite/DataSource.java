package sqlite;
import modules.BaseModel;

import java.util.List;

public interface DataSource {
    BaseModel insert(BaseModel obj);
    BaseModel delete(BaseModel obj);
    BaseModel update(BaseModel obj);
    BaseModel get(BaseModel obj);
    BaseModel authenticate(String username, String password);
//    BaseModel getUser(String userName);

    List <BaseModel> queryList(BaseModel obj, String filter);
    BaseModel query(BaseModel obj, String filter);
}
