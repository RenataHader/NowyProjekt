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
    private final GameClient client = new GameClient();

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NickInputView.fxml"));
            Parent root = loader.load();
            NickInputController controller = loader.getController();
            controller.setSelectedGame(selectedGame);

            Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

