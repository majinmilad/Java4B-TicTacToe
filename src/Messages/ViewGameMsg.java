package Messages;

import modules.Game;
import modules.GameViewers;

public class ViewGameMsg extends Message {

    private GameViewers newViewer;

    public ViewGameMsg(GameViewers newViewer) {
        super("ViewGameMsg", "");

        this.newViewer = newViewer;
    }

    public GameViewers getNewViewer()
    {
        return newViewer;
    }

}
