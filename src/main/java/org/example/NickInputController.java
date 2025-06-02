package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class NickInputController {

    @FXML private Label titleLabel;
    @FXML private TextField nicknameField;
    @FXML private TextField playerCountField;
    @FXML private HBox playerCountBox;

    private String selectedGame; // MEMORY / CHARADES

    public void setSelectedGame(String selectedGame) {
        this.selectedGame = selectedGame;

        if ("CHARADES".equalsIgnoreCase(selectedGame)) {
            playerCountBox.setVisible(true);
            titleLabel.setText("Podaj nick i liczbę graczy");
        } else if ("MEMORY".equalsIgnoreCase(selectedGame)){
            playerCountBox.setVisible(false);
            titleLabel.setText("Podaj nick");
        }
    }

    @FXML
    public void submit(ActionEvent event) {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            showAlert("Wpisz nick!");
            return;
        }

        GameClient client = GameClient.getInstance();
        try {
            client.connect("localhost", 12345);
            client.sendMessage("NICK:" + nickname);
            ViewManager manager = ViewManager.getInstance();

            if ("CHARADES".equalsIgnoreCase(selectedGame)) {
                String count = playerCountField.getText().trim();
                int playerCount = Integer.parseInt(count);
                if (!count.matches("\\d+") && playerCount > 1) {
                    showAlert("Liczba graczy musi być liczba większa od jednego!");
                    return;
                }
                client.sendMessage("GAME:CHARADES:" + count);
                manager.changeView("charadesDraw");
            } else {
                client.sendMessage("GAME:MEMORY");
                manager.changeView("memory");
            }
        } catch (Exception e) {
            showAlert("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
