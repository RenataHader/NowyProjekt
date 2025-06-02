package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GameSelectionController {
    private final GameClient client = GameClient.getInstance();

    @FXML
    public void initialize() {
        try {
            client.connect("localhost", 12345);
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    @FXML
    public void startMemoryGame(ActionEvent event) {
        loadNickInputView("MEMORY", event);
    }

    @FXML
    public void startCharadesGame(ActionEvent event) {
        loadNickInputView("CHARADES", event);
    }

    private void loadNickInputView(String selectedGame, ActionEvent event) {
        ViewManager manager = ViewManager.getInstance();
        NickInputController controller = manager.getController("nickInput", NickInputController.class);
        controller.setSelectedGame(selectedGame);
        manager.changeView("nickInput");
    }
}

