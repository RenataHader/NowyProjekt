package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class MemoryController {
    @FXML private GridPane gameGrid;
    @FXML private TextArea chatArea;
    @FXML private Label scoreLabel;
    @FXML private Label player1Score;
    @FXML private Label player2Score;
    @FXML private Label turnLabel;


    private final Button[][] buttons = new Button[3][8];
    private GameClient client = GameClient.getInstance();

    private final Image cardBackImage = new Image(getClass().getResource("/images/card_back.png").toExternalForm());
    private final ImageView[][] cardViews = new ImageView[3][8];

    public void updateTurn(int playerNumber) {
        if (turnLabel != null) {
            turnLabel.setText("Tura gracza " + playerNumber);
        }
    }


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
                btn.setPrefSize(60, 90);
                btn.setFocusTraversable(false);
                btn.setStyle("-fx-background-color: transparent;");

                ImageView imgView = new ImageView(cardBackImage);
                imgView.setFitWidth(60);
                imgView.setFitHeight(90);
                imgView.setPreserveRatio(false);

                btn.setGraphic(imgView);
                buttons[i][j] = btn;
                cardViews[i][j] = imgView;

                int row = i, col = j;
                btn.setOnAction(e -> client.sendMessage(row + "," + col));

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

        String imagePath = "/images/card_" + value + ".png";
        Image frontImage = new Image(getClass().getResource(imagePath).toExternalForm());
        cardViews[r][c].setImage(frontImage);
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
            cardViews[r][c].setImage(cardBackImage);
            buttons[r][c].setDisable(false);
        }
    }


    public void setScore(String msg) {
        msg = msg.replace("Server: ", "").replace("Wynik - ", "");
        String[] parts = msg.split(" ");
        int p1 = Integer.parseInt(parts[2]);
        int p2 = Integer.parseInt(parts[5]);

        player1Score.setText("Gracz 1: " + p1);
        player2Score.setText("Gracz 2: " + p2);
    }

    public void setServer(String msg) {
        if (msg.startsWith("Server: Tura gracza ")) {
            int playerNumber = Integer.parseInt(msg.substring("Server: Tura gracza ".length()).trim());
            updateTurn(playerNumber);
        } else {
            chatArea.appendText(msg + "\n");
        }
    }

}