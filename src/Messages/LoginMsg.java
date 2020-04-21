package Messages;

public class LoginMsg extends Message
{
    private String username;
    private String password;

    public LoginMsg(String username, String password)
    {
        super("loginMsg", "");
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
