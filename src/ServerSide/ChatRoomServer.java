//package ServerSide;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.concurrent.ArrayBlockingQueue;
//
//public class ChatRoomServer implements Runnable
//{
//    //DATA MEMBERS
//    private ServerSocket ss;
//    private int chatRoomPort;
//    private final String chatRoomName;
//    private Thread chatRoomThread;
//    private int numInRoom = 0;
//
//    private ArrayList<MemberConnection> membersInTheRoom = new ArrayList<>();
//    private ArrayBlockingQueue<Message> msgQueue = new ArrayBlockingQueue<>(1000);
//    private ArrayList<ChatMsg> chatHistory = new ArrayList<>();
//
//
//    //METHODS
//    ChatRoomServer(String roomName)
//    {
//        chatRoomName = roomName;
//
//        try
//        {
//            //establish chat room on an available port
//            ss = new ServerSocket(0);
//            chatRoomPort = ss.getLocalPort();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        chatRoomThread = new Thread(this);
//        chatRoomThread.start();
//    }
//
//    @Override
//    public void run()
//    {
//        //turn on message publisher object for the whole room
//        new Thread(new Publisher()).start();
//
//        try
//        {
//            //chat room open for connections to new members
//            while(true)
//            {
//                Socket socket = ss.accept(); //blocking thread
//
//                ++numInRoom;
//
//                displayConnectionStatus(socket);
//
//                membersInTheRoom.add(new MemberConnection(socket));
//            }
//        }
//        catch (IOException e) {
//            System.out.println("exception caught in " + chatRoomName + "'s run()");
//            e.printStackTrace();
//        }
//    }
//
//
//    private class MemberConnection implements Runnable
//    {
//        private Socket socketToMember;
//        private String memberName;
//        private Thread thisThread;
//
//        ObjectInputStream objectInputFromMember;
//        ObjectOutputStream objectOutputToMember;
//
//        MemberConnection(Socket socket)
//        {
//            socketToMember = socket;
//
//            //Wrap the IO streams
//            try
//            {
//                objectInputFromMember = new ObjectInputStream(socketToMember.getInputStream());
//                objectOutputToMember = new ObjectOutputStream(socketToMember.getOutputStream());
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            //Send this connection on its own thread
//            thisThread = new Thread(this);
//            thisThread.start();
//        }
//
//        @Override
//        public void run()
//        {
//            try
//            {
//                //read in new client's name
//                memberName = objectInputFromMember.readUTF();
//
//                //push member-has-joined and chat-history messages
//                msgQueue.add(new JoinedChatMsg(memberName));
//                msgQueue.add(new ChatHistoryMsg(chatHistory, chatRoomName, memberName));
//
//                //Receive messages from clients
//                while(true)
//                {
//                    //receive messages
//                    Message incomingMsg = (Message) objectInputFromMember.readObject();
//
//                    if(incomingMsg instanceof ChatMsg)
//                    {
//                        ChatMsg receivedMsg = (ChatMsg) incomingMsg;
//
//                        System.out.println("Chat Room \"" + chatRoomName + "\" RECEIVED : " + receivedMsg.txt + " {from " + memberName + "}\n");
//
//                        //repackage msg to ensure proper sentBy value
//                        receivedMsg = new ChatMsg(receivedMsg, memberName);
//
//                        //store message in blocking queue for publisher
//                        msgQueue.add(receivedMsg);
//                    }
//                    else if(incomingMsg instanceof LeftChatMsg)
//                    {
//                        LeftChatMsg userLeftMsg = (LeftChatMsg) incomingMsg;
//
//                        msgQueue.add(userLeftMsg);
//                    }
//                }
//            }
//            catch(IOException | ClassNotFoundException e)
//            {
//                //if socket with member throws IOException it has lost connection
//                //remove member from member list and notify others in room
//                if(e instanceof IOException)
//                {
//                    msgQueue.add(new LeftChatMsg(memberName));
//                    membersInTheRoom.remove(this);
//                }
//
//                System.out.println("exception caught in run() of "  + memberName + "'s MemberConnection object");
//                e.printStackTrace();
//            }
//        }
//
//        void sendMessageFromPublisher(Message msg)
//        {
//            try
//            {
//                objectOutputToMember.writeObject(msg);
//            }
//            catch (IOException e) {
//                System.out.println("exception caught in sendMessageFromPub() of " + memberName + "'s MemberConnection object");
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private class Publisher implements Runnable
//    {
//        @Override
//        public void run()
//        {
//            while(true)
//            {
//                try
//                {
//                    //pull message off queue
//                    Message nextMsg = msgQueue.take();
//
//                    //handle chat history messages differently
//                    if(nextMsg instanceof ChatHistoryMsg)
//                    {
//                        ChatHistoryMsg historyMsg = (ChatHistoryMsg) nextMsg;
//
//                        for (MemberConnection client : membersInTheRoom)
//                        {
//                            if(historyMsg.receivingName.equals(client.memberName) && client.thisThread.isAlive())
//                            {
//                                client.sendMessageFromPublisher(nextMsg);
//                                break;
//                            }
//                        }
//                    }
//                    else
//                    {
//                        //propagate message to appropriate clients
//                        for (MemberConnection client : membersInTheRoom)
//                        {
//                            if(/*!client.memberName.equals(nextMsg.doNotSendTo) && */client.thisThread.isAlive())
//                                client.sendMessageFromPublisher(nextMsg);
//                        }
//                    }
//
//                    //store chat messages in chatHistory
//                    if(nextMsg instanceof ChatMsg)
//                        chatHistory.add((ChatMsg) nextMsg);
//                }
//                catch (InterruptedException e) {
//                    System.out.println("exception caught in Publisher's run()");
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    /*********************************************************************************/
//
//
//    private void displayConnectionStatus(Socket socket)
//    {
//        if(socket.isConnected())
//        {
//            System.out.println("Room-Member connection established...");
//            System.out.println("Room: " + chatRoomName);
//            System.out.println("(Connected to " + socket.getPort() + " [IP: " + socket.getInetAddress().getHostAddress() + "])\n");
//        }
//        else
//            System.out.println("-- SOCKET NOT CONNECTED --\n");
//    }
//
//
//    int getChatRoomPort() {return chatRoomPort;}
//}
