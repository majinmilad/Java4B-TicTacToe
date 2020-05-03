package Messages;

import modules.User;

public class KillListenerMsg extends Message
{
    private String message; //for debugging purposes only

    public KillListenerMsg()
    {
        super("KillListenerMsg", "");
    }

    public KillListenerMsg(String message)
    {
        super("KillListenerMsg", "");
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
