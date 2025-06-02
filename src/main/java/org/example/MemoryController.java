package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class MemoryController {
    @FXML private GridPane gameGrid;
    @FXML private TextArea chatArea;
    @FXML private Label scoreLabel;


    private final Button[][] buttons = new Button[3][8];
    private GameClient client = GameClient.getInstance();

    @FXML
    public void initialize() {
        initGrid();
        try {
            client.connect("localhost", 12345);
            client.sendMessage("GAME:MEMORY");
          // client.setOnMessage(this::handleMessage);
        } catch (Exception e) {
            chatArea.appendText("Connection failed: " + e.getMessage() + "\n");
        }
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

    public void turnCard(String msg) {
        String[] parts = msg.substring(7).split("=");
        String[] coords = parts[0].split(",");
        int r = Integer.parseInt(coords[0]);
        int c = Integer.parseInt(coords[1]);
        String value = parts[1];
        buttons[r][c].setText(value);
        buttons[r][c].setDisable(true);
    }

    public void matchCard(String msg) {
        for (String pos : msg.substring(6).split("\\|")) {
            String[] coords = pos.split(",");
            int r = Integer.parseInt(coords[0]);
            int c = Integer.parseInt(coords[1]);
            buttons[r][c].setStyle("-fx-background-color: lightgreen;");
            buttons[r][c].setDisable(true);
        }
    }

    public void backCard(String msg) {
        for (String pos : msg.substring(5).split("\\|")) {
            String[] coords = pos.split(",");
            int r = Integer.parseInt(coords[0]);
            int c = Integer.parseInt(coords[1]);
            buttons[r][c].setText("");
            buttons[r][c].setDisable(false);
        }
    }

    public void setScore(String msg) {
        scoreLabel.setText(msg.replace("Server: ", ""));
    }

    public void setServer(String msg) {
        chatArea.appendText(msg + "\n");
    }
}
