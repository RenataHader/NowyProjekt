package org.example;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


public class MemoryController {
    @FXML private GridPane gameGrid;
    @FXML private TextArea chatArea;
    @FXML private Label scoreLabel;
    @FXML private Label player1Score;
    @FXML private Label player2Score;
    @FXML private Label turnLabel;


    private final Button[][] buttons = new Button[3][8];
    private final ImageView[][] cardImages = new ImageView[3][8];
    private final GameClient client = GameClient.getInstance();
    private Image backImage;

    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;

    private String player1Name = "";
    private String player2Name = "";

    @FXML
    public void initialize() {
        backImage = new Image(getClass().getResource("/images/card_back.png").toExternalForm());
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
        gameGrid.getChildren().clear();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                ImageView imageView = new ImageView(backImage);
                imageView.setFitWidth(CARD_WIDTH);
                imageView.setFitHeight(CARD_HEIGHT);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(false);

                // Przezroczysty przycisk nad obrazkiem
                Button invisibleButton = new Button();
                invisibleButton.setOpacity(0);
                invisibleButton.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
                invisibleButton.setFocusTraversable(false);

                int row = i, col = j;
                invisibleButton.setOnAction(e -> client.sendMessage(row + "," + col));

                StackPane cardPane = new StackPane(imageView, invisibleButton);
                buttons[i][j] = invisibleButton;
                cardImages[i][j] = imageView;

                gameGrid.add(cardPane, j, i);
            }
        }
    }

    private void flipCard(int row, int col, Image frontImage, boolean showFront) {
        ImageView view = cardImages[row][col];

        RotateTransition flipOut = new RotateTransition(Duration.millis(150), view);
        flipOut.setFromAngle(0);
        flipOut.setToAngle(90);

        RotateTransition flipIn = new RotateTransition(Duration.millis(150), view);
        flipIn.setFromAngle(270);
        flipIn.setToAngle(360);

        flipOut.setOnFinished(event -> {
            if (showFront) {
                view.setImage(frontImage);
            } else {
                view.setImage(backImage);
            }
            flipIn.play();
        });

        flipOut.play();
    }

    public void turnCard(String msg) {
        String[] parts = msg.substring(7).split("=");
        String[] coords = parts[0].split(",");
        int r = Integer.parseInt(coords[0]);
        int c = Integer.parseInt(coords[1]);
        int cardId = Integer.parseInt(parts[1]);

        Image frontImage = new Image(getClass().getResource("/images/card_" + cardId + ".png").toExternalForm());
        flipCard(r, c, frontImage, true);
        buttons[r][c].setDisable(true);
    }


    public void matchCard(String msg) {
        for (String pos : msg.substring(6).split("\\|")) {
            String[] coords = pos.split(",");
            int r = Integer.parseInt(coords[0]);
            int c = Integer.parseInt(coords[1]);
            buttons[r][c].setStyle("-fx-border-color: lightgreen; -fx-border-width: 3;");
        }
    }

    public void backCard(String msg) {
        for (String pos : msg.substring(5).split("\\|")) {
            String[] coords = pos.split(",");
            int r = Integer.parseInt(coords[0]);
            int c = Integer.parseInt(coords[1]);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.4));
            pause.setOnFinished(e -> {
                flipCard(r, c, null, false);
                buttons[r][c].setDisable(false);
            });
            pause.play();
        }
    }


    public void setScore(String msg) {
        msg = msg.replace("Server: ", "").replace("Wynik - ", "").trim();

        // Przykład: "Jan: 3 Anna: 4"
        String[] parts = msg.split(" ");

        if (parts.length >= 4) {
            String player1Name = parts[0].replace(":", "");
            int player1Points = Integer.parseInt(parts[1]);

            String player2Name = parts[2].replace(":", "");
            int player2Points = Integer.parseInt(parts[3]);

            player1Score.setText(player1Name + ": " + player1Points);
            player2Score.setText(player2Name + ": " + player2Points);
        }
    }

    public void setServer(String msg) {
        if (msg.startsWith("Server: Tura gracza ")) {
            String playerName = msg.substring("Server: Tura gracza ".length()).trim();
            updateTurn(playerName);
        } else {
            chatArea.appendText(msg + "\n");
        }
    }

    public void updateTurn(String playerNumber) {
        if (turnLabel != null) {
            turnLabel.setText("Tura gracza " + playerNumber);
        }
    }

    public void setNick(String msg) {
        if (msg.startsWith("NICKI:")) {
            String[] names = msg.substring(6).split(",");
            if (names.length == 2) {
                player1Name = names[0];
                player2Name = names[1];

                player1Score.setText(player1Name + ": 0");
                player2Score.setText(player2Name + ": 0");
            }
        }
    }
}