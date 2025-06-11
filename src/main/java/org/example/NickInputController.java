package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NickInputController {

    @FXML private Label titleLabel;
    @FXML private Label playerLabel;
    @FXML private TextField nicknameField;
    @FXML private TextField playerCountField;

    private String selectedGame;

    public void setSelectedGame(String selectedGame) {
        this.selectedGame = selectedGame;

        if ("CHARADES".equalsIgnoreCase(selectedGame)) {
            playerCountField.setVisible(true);
            titleLabel.setText("Podaj nick i liczbę graczy");
            playerLabel.setVisible(true);
            playerLabel.setText("Liczba graczy 2 - 6:");
        } else if ("MEMORY".equalsIgnoreCase(selectedGame)) {
            playerCountField.setVisible(false);
            titleLabel.setText("Podaj nick");
            playerLabel.setVisible(false);
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        ViewManager manager = ViewManager.getInstance();
        manager.changeView("main");
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            showAlert("Wpisz nick!");
            return;
        }

        if ("CHARADES".equalsIgnoreCase(selectedGame)) {
            String countText = playerCountField.getText().trim();
            if (countText.isEmpty()) {
                showAlert("Wprowadź liczbę graczy!");
                return;
            }

            try {
                int playerCount = Integer.parseInt(countText);
                if (playerCount < 2 || playerCount > 6) {
                    showAlert("Liczba graczy musi być między 2 a 6!");
                    return;
                }

                proceedToGame(nickname, playerCount);

            } catch (NumberFormatException e) {
                showAlert("Liczba graczy musi być cyfrą!");
                return;
            }
        } else {
            proceedToGame(nickname, 0);
        }
    }

    private void proceedToGame(String nickname, int playerCount) {
        try {
            GameClient client = GameClient.getInstance();
            NetworkConfigReader config = new NetworkConfigReader();
            client.connect(config.getIp(), config.getPort());
            client.sendMessage("NICK:" + nickname);

            ViewManager manager = ViewManager.getInstance();

            if ("CHARADES".equalsIgnoreCase(selectedGame)) {
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uwaga");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
