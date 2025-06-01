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
        } else {
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
                if (!count.matches("\\d+")) {
                    showAlert("Liczba graczy musi być liczbą!");
                    return;
                }
                client.sendMessage("GAME:CHARADES:" + count);


                manager.changeView("charadesDraw");
            } else {
                client.sendMessage("GAME:MEMORY");
                MemoryController controller = manager.getController("memory", MemoryController.class);
                controller.setClient(client);
                manager.changeView("memory");
            }
        } catch (Exception e) {
            showAlert("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }

    private void loadFXML(String path, GameClient client, ActionEvent event) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
//        Parent root = loader.load();
        ViewManager manager = ViewManager.getInstance();
        Parent view = manager.getView("charadesDraw");

        if (path.contains("memory")) {
//            MemoryController ctrl = loader.getController();
//            ctrl.setClient(client);
        } else {

        }

//        Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(view));
//        stage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
