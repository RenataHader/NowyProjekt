package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


import java.io.ByteArrayInputStream;
import java.util.*;

public class CharadesGuessController implements GameGUIController{

    @FXML private ImageView drawingImage;
    @FXML private TextField guessField;
    @FXML private Label resultLabel;
    @FXML private ImageView backgroundImage;
    @FXML private Label timerLabel;
    @FXML private VBox scorePanel;
    private Timeline countdownTimeline;
    private int remainingSeconds = 30;

    private final Map<String, String> base64Map = new HashMap<>();
    private final List<String> senderOrder = new ArrayList<>();
    private int currentIndex = 0;
    private String selectedSender;
    private GameClient client = GameClient.getInstance();

    @FXML
    public void initialize() {
        // Bind background image to full window
        Platform.runLater(() -> {
            backgroundImage.fitWidthProperty().bind(backgroundImage.getScene().widthProperty());
            backgroundImage.fitHeightProperty().bind(backgroundImage.getScene().heightProperty());
        });
    }

    public void processShowPicture(String msg) {
        String[] parts = msg.split(":", 3);
        if (parts.length == 3) {
            String sender = parts[1];
            String base64 = parts[2];

            if (!base64Map.containsKey(sender)) {
                base64Map.put(sender, base64);
                senderOrder.add(sender);

                // Pierwszy rysunek? Pokaż od razu
                if (senderOrder.size() == 1) {
                    showCurrentPicture();
                }
            }
        }
    }

    private void showCurrentPicture() {
        if (currentIndex < senderOrder.size()) {
            selectedSender = senderOrder.get(currentIndex);
            String base64 = base64Map.get(selectedSender);
            if (base64 != null) {
                byte[] bytes = Base64.getDecoder().decode(base64);
                drawingImage.setImage(new Image(new ByteArrayInputStream(bytes)));
                resultLabel.setText("Zgadnij rysunek gracza: " + selectedSender);
                startTurnTimer();
            }
        } else {
            resultLabel.setText("Wszystkie rysunki odgadniete! 🎉");
            drawingImage.setImage(null);
            selectedSender = null;

            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
        }
    }

    @FXML
    public void submitGuess() {
        if (selectedSender == null || guessField.getText().isEmpty()) {
            resultLabel.setText("Wpisz odpowiedź.");
            return;
        }

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        String guess = guessField.getText().trim();
        client.sendMessage("GUESS:" + selectedSender + ":" + guess);
        guessField.clear();
        currentIndex++;
        showCurrentPicture();
    }

    public void prepareNewRound() {
        base64Map.clear();
        senderOrder.clear();
        currentIndex = 0;
        selectedSender = null;
        resultLabel.setText("");
        drawingImage.setImage(null);
    }

    public void startTurnTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        remainingSeconds = 30;
        timerLabel.setText("Czas: " + remainingSeconds + "s");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            timerLabel.setText("Czas: " + remainingSeconds + "s");

            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                autoSkipGuess(); // automatyczne przejście
            }
        }));

        countdownTimeline.setCycleCount(remainingSeconds);
        countdownTimeline.play();
    }

    private void autoSkipGuess() {
        if (selectedSender == null) return;

        // Możesz wysłać info o pominięciu, albo nie — zależnie od potrzeb
        client.sendMessage("GUESS:" + selectedSender + ":BRAK");

        guessField.clear();
        currentIndex++;
        showCurrentPicture();
    }

    public void setScore(String data) {
        scorePanel.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().contains(":"));

        String[] entries = data.split(",");
        for (String entry : entries) {
            if (!entry.isBlank()) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    String nick = parts[0];
                    String points = parts[1];

                    Label label = new Label(nick + ": " + points + " pkt");
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
                    scorePanel.getChildren().add(label);
                }
            }
        }
    }
    public void setNick(String data) {
        scorePanel.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().contains(":"));

        String[] nicki = data.split(",");
        for (String nick : nicki) {
            if (!nick.isBlank()) {
                Label label = new Label(nick + ": 0 pkt");
                label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
                scorePanel.getChildren().add(label);
            }
        }
    }
}
