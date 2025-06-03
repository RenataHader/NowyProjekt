package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NickInputController {

    @FXML private Label titleLabel;
    @FXML private TextField nicknameField;
    @FXML private ChoiceBox<Integer> playerCountChoice;

    private String selectedGame; // MEMORY / CHARADES

    public void setSelectedGame(String selectedGame) {
        this.selectedGame = selectedGame;

        if ("CHARADES".equalsIgnoreCase(selectedGame)) {
            playerCountChoice.setVisible(true);
            titleLabel.setText("Podaj nick i liczbę graczy");
        } else if ("MEMORY".equalsIgnoreCase(selectedGame)) {
            playerCountChoice.setVisible(false);
            titleLabel.setText("Podaj nick");
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
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
                Integer playerCount = playerCountChoice.getValue();
                if (playerCount == null || playerCount < 2) {
                    showAlert("Wybierz poprawną liczbę graczy (min. 2)!");
                    return;
                }
                client.sendMessage("GAME:CHARADES:" + playerCount);
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