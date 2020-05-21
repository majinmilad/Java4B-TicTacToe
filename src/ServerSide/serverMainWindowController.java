package ServerSide;

import Messages.*;
import app.gameHistoryController;
import app.movesWindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import modules.BaseModel;
import modules.Game;
import modules.GameViewers;
import modules.User;
import sqlite.DatabaseManager;
import javafx.scene.control.TableCell;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class serverMainWindowController implements Observer, Initializable {

    @FXML
    private TextArea messageBox;

    @FXML
    private TextArea onlineUserBox;

    @FXML
    private Button modifyUserButton;

    @FXML
    private TextArea gameMessageBox;

    @FXML
    private Button gameInformationButton;

    @FXML
    private TableView<Game> gameList;

    @FXML
    private TableColumn<Game, String> idCol;

    @FXML
    private TableColumn<Game, String> statusCol;

    @FXML
    private TableColumn<Game, String> infoCol;

    @FXML
    private TabPane tabP;

    @FXML
    private Button updateGamesButton;

    public ObservableList<Game> list = FXCollections.observableArrayList();


    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        gameInformationButton.setDisable(true);
        setColumns();
        colorColumns();
        gameList.setItems(list);
        Server server = Server.getInstance();
        server.addObserver(this);
        refreshButtonClicked(new ActionEvent());
    }

    private void colorColumns() {
        statusCol.setCellFactory(new Callback<TableColumn<Game,String>, TableCell<Game,String>>() {
            public TableCell<Game,String> call(TableColumn<Game,String> param) {
                return new TableCell<Game,String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            this.setTextFill(Color.RED);
                            // Get fancy and change color based on data
                            if(item.contains("RUNNING")) {
                                this.setTextFill(Color.GREEN);
                            }
                            else if (item.contains("WAITING"))
                            {
                                this.setTextFill(Color.BLUE);
                            }

                            this.setFont(Font.font("System", FontWeight.BOLD , 12));

                            setText(item);
                        }
                    }
                };
            }
        });
    }

    private void setColumns() {
        ArrayList<BaseModel> completedGames = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new Game(), "ORDER BY gameStatus DESC");
        for (BaseModel games : completedGames)
        {
            list.add((Game) games);
        }

        idCol.setCellValueFactory(new PropertyValueFactory<Game,String>("gameId"));
        infoCol.setCellValueFactory(new PropertyValueFactory<Game,String>("startTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<Game,String>("status"));
        statusCol.setStyle( "-fx-alignment: CENTER;");
    }

    @FXML
    void addUserClicked(ActionEvent event) {

    }

    @FXML
    void modifyUserButtonClicked(ActionEvent event) throws IOException
    {
        // open modify users in new window
        Parent modifyUsersRoot = FXMLLoader.load(getClass().getResource("serverModifyUsers.fxml"));
        Scene modifyUsersScene = new Scene(modifyUsersRoot);
        Stage window = new Stage();
        window.setScene(modifyUsersScene);
        window.show();
    }

    @FXML
    void refreshButtonClicked(ActionEvent event)
    {
        // Online user list refresh
        onlineUserBox.clear();

        List<BaseModel> list = DatabaseManager.getInstance().queryList(new User(), "AND status = 'ONLINE'");

        for (BaseModel u : list)
        {
            onlineUserBox.appendText(((User) u).getUsername() + "\n");
        }

        // Game Table refresh

    }

    @FXML
    void selectGame()
    {
        try{
            gameList.getSelectionModel().getSelectedItem();
            gameInformationButton.setDisable(false);

        } catch (NullPointerException e){
            gameInformationButton.setDisable(true);
            System.out.println("Server clicked on Empty Cell");
        }
    }

    @FXML
    void gameInfoButtonClicked() throws IOException {
        Game selectedGame = gameList.getSelectionModel().getSelectedItem();
        FXMLLoader loader = new FXMLLoader(gameInfoController.class.getResource("gameInfoWindow.fxml"));
        Parent gameInfoParent = loader.load();

        //set players involved in the controller
        gameInfoController setController = loader.getController();


        User p1 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP1Id() + "\' ");
        User p2 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP2Id() + "\' ");

        setController.initWindows(selectedGame,p1,p2);

        Scene gameInfoScene = new Scene(gameInfoParent);
        Stage gameInfoStage = new Stage();
        gameInfoStage.setScene(gameInfoScene);
        gameInfoStage.show();
    }

    @FXML
    void updateGamesButtonClicked()
    {
        gameInformationButton.setDisable(true);
        gameList.getItems().clear();
        setColumns();
        colorColumns();
        gameList.setItems(list);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                if(arg instanceof RegistrationMsg)
                {
                    messageBox.appendText("\"" + ((RegistrationMsg) arg).getUser().getUsername() + "\" has registered as a new user\n\n");
                }
                else if(arg instanceof LoginMsg)
                {
                    messageBox.appendText("user \"" + ((LoginMsg) arg).getUsername() + "\" has just logged on\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof DeleteUserMsg)
                {
                    messageBox.appendText("user \"" + ((DeleteUserMsg) arg).getUser().getUsername() + "\" deleted their account (account deactivated)\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof LogoutMsg)
                {
                    messageBox.appendText("user \"" + ((LogoutMsg) arg).getUser().getUsername() + "\" has logged out\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof ReactivateUserMsg)
                {
                    messageBox.appendText("user \"" + ((ReactivateUserMsg) arg).getUser().getUsername() + "\" has reactivated their deleted account\n\n");
                }
                else if(arg instanceof UpdateUserMsg)
                {
                    messageBox.appendText("user \"" + ((UpdateUserMsg) arg).getUser().getUsername() + "\" has updated their account\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
            }
        });
    }
}
