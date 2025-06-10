package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class EndGameController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label winnerLabel;

    private String winnerName;

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
        if (winnerLabel != null) {
            winnerLabel.setText("Zwycięzca: " + winnerName);
        }
    }

    @FXML
    public void initialize() {
        // Inicjalizacja kontrolera
        if (winnerName != null && winnerLabel != null) {
            winnerLabel.setText("Zwycięzca: " + winnerName);
        }
    }

    @FXML
    private void onYesClicked() {
        // Gracz chce zagrać ponownie - przejdź do wyboru gry
        goToGameSelectionView();
    }

    @FXML
    private void onNoClicked() {
        // Gracz nie chce grać - zamknij aplikację
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    private void goToGameSelectionView() {
        try {
            ViewManager manager = ViewManager.getInstance();
            manager.changeView("main");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}