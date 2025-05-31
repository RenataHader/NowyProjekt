package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class MemoryController {
    @FXML private GridPane gameGrid;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Label scoreLabel;


    private final Button[][] buttons = new Button[3][8];
    private GameClient client;

    @FXML
    public void initialize() {
        initGrid();
        try {
            client.connect("localhost", 12345);
            client.sendMessage("GAME:MEMORY"); // <== DODAJ TUTAJ
            client.setOnMessage(this::handleMessage);
        } catch (Exception e) {
            chatArea.appendText("Connection failed: " + e.getMessage() + "\n");
        }
    }

    public void setClient(GameClient client) {
        this.client = client;
        client.setOnMessage(this::handleMessage);
    }

    private void initGrid() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                Button btn = new Button();
                btn.setPrefSize(50, 50);
                btn.setFocusTraversable(false);
                int row = i, col = j;
                btn.setOnAction(e -> client.sendMessage(row + "," + col));
                buttons[i][j] = btn;
                gameGrid.add(btn, j, i);
            }
        }
    }

    private void handleMessage(String msg) {
        Platform.runLater(() -> {
            if (msg.startsWith("UPDATE:")) {
                String[] parts = msg.substring(7).split("=");
                String[] coords = parts[0].split(",");
                int r = Integer.parseInt(coords[0]);
                int c = Integer.parseInt(coords[1]);
                String value = parts[1];
                buttons[r][c].setText(value);
                buttons[r][c].setDisable(true);
            } else if (msg.startsWith("MATCH:")) {
                for (String pos : msg.substring(6).split("\\|")) {
                    String[] coords = pos.split(",");
                    int r = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);
                    buttons[r][c].setStyle("-fx-background-color: lightgreen;");
                    buttons[r][c].setDisable(true);
                }
            } else if (msg.startsWith("HIDE:")) {
                for (String pos : msg.substring(5).split("\\|")) {
                    String[] coords = pos.split(",");
                    int r = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);
                    buttons[r][c].setText("");
                    buttons[r][c].setDisable(false);
                }
            } else if (msg.startsWith("Server: Wynik - ")) {
                scoreLabel.setText(msg.replace("Server: ", ""));
            } else if (msg.startsWith("Server:")) {
                chatArea.appendText(msg + "\n");
            }
            else {
                chatArea.appendText(msg + "\n");
            }
        });
    }

    @FXML
    public void sendMessage() {
        String text = messageField.getText();
        client.sendMessage(text);
        messageField.clear();
    }
}
