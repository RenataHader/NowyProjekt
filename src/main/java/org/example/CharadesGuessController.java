package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.*;

public class CharadesGuessController {

    @FXML private ImageView drawingImage;
    @FXML private TextField guessField;
    @FXML private Label resultLabel;

    private final Map<String, String> base64Map = new HashMap<>();
    private final List<String> senderOrder = new ArrayList<>();
    private int currentIndex = 0;
    private String selectedSender;
    private GameClient client = GameClient.getInstance();

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
            }
        } else {
            resultLabel.setText("Wszystkie rysunki odgadniete! 🎉");
            drawingImage.setImage(null);
            selectedSender = null;
        }
    }

    @FXML
    public void submitGuess() {
        if (selectedSender == null || guessField.getText().isEmpty()) {
            resultLabel.setText("Wpisz odpowiedź.");
            return;
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

}
