package Messages;

import javafx.beans.binding.MapExpression;
import modules.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;


//registration messages
//game move messages
//game result messages
//view game messages


public class Message implements Serializable
{
    final String messageType;
    final String originalSender;
    private UUID connectionID;
    final String doNotSendTo;

    public Message(String msgType, String sender)
    {
        messageType = msgType;
        this.originalSender = sender;
        doNotSendTo = sender;
    }

    public UUID getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(UUID id) {
        this.connectionID = id;
    }
}


class ChatRoomInfoMsg extends Message
{
    final int chatRoomPort;
    final String chatRoomName;
    final boolean hadToBeCreated;

    ChatRoomInfoMsg(int portNum, String roomName, boolean isBrandNewRoom)
    {
        super("ChatRoomInfo", "SeverManager");
        chatRoomPort = portNum;
        chatRoomName = roomName;
        hadToBeCreated = isBrandNewRoom;
    }
}


class JoinedChatMsg extends Message
{
    JoinedChatMsg(String userName)
    {
        super("JoinedChatMsg", userName);
    }
}


class LeftChatMsg extends Message
{
    LeftChatMsg(String userName)
    {
        super("LeftChatMsg", userName);
    }
}


class ChatMsg extends Message
{
    final String txt;

    ChatMsg(String txt, String userName)
    {
        super("ChatMsg", userName);
        this.txt = txt;
    }

    //copy constructor but resets sentBy
    ChatMsg(ChatMsg srcObj, String messageFrom)
    {
        this(srcObj.txt, messageFrom);
    }
}


class ChatHistoryMsg extends Message
{
    ArrayList<ChatMsg> chatHistory;
    String receivingName;

    ChatHistoryMsg(ArrayList<ChatMsg> chatLog, String roomName, String sendTo)
    {
        super("ChatHistoryMsg", roomName);
        chatHistory = (ArrayList<ChatMsg>) chatLog.clone();
        receivingName = sendTo;
    }
}


class ChatGUIExited extends Message
{
    ChatGUIExited()
    {
        super("ChatGUIExited", "GUI");
    }
}
