package org.example;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class CharadesDrawController implements GameGUIController{

    @FXML private Canvas drawingCanvas;
    @FXML private Button sendDrawingButton;
    @FXML private ImageView backgroundImage;
    @FXML private ColorPicker colorPicker;
    @FXML private ToggleButton eraserToggle;
    @FXML private Slider sizeSlider;
    @FXML private Button clearButton;
    @FXML private ToggleButton drawLine;
    @FXML private ToggleButton drawRect;
    @FXML private ToggleButton drawOval;
    @FXML private Label timerLabel;
    @FXML private Label wordLabel;
    @FXML private VBox scorePanel;

    private Timeline countdownTimeline;
    private int remainingSeconds = 60;

    private enum Tool {
        FREE, LINE, RECT, OVAL
    }

    private Tool currentTool = Tool.FREE;
    private double startX, startY;
    private boolean erasing = false;
    private GraphicsContext gc;

    private final GameClient client = GameClient.getInstance();

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        gc.setLineWidth(sizeSlider.getValue());
        colorPicker.setValue(Color.BLACK);
        gc.setStroke(Color.BLACK);

        Platform.runLater(() -> {
            backgroundImage.fitWidthProperty().bind(backgroundImage.getScene().widthProperty());
            backgroundImage.fitHeightProperty().bind(backgroundImage.getScene().heightProperty());
        });

        ToggleGroup toolsGroup = new ToggleGroup();
        drawLine.setToggleGroup(toolsGroup);
        drawRect.setToggleGroup(toolsGroup);
        drawOval.setToggleGroup(toolsGroup);
        eraserToggle.setToggleGroup(toolsGroup);

        drawLine.setOnAction(e -> { currentTool = Tool.LINE; updateToggleStyles(); });
        drawRect.setOnAction(e -> { currentTool = Tool.RECT; updateToggleStyles(); });
        drawOval.setOnAction(e -> { currentTool = Tool.OVAL; updateToggleStyles(); });
        eraserToggle.setOnAction(e -> {
            erasing = eraserToggle.isSelected();
            currentTool = Tool.FREE;
            updateToggleStyles();
        });

        sizeSlider.valueProperty().addListener((obs, o, n) -> gc.setLineWidth(n.doubleValue()));

        colorPicker.setOnAction(e -> {
            if (!eraserToggle.isSelected()) {
                gc.setStroke(colorPicker.getValue());
            }
        });

        clearButton.setOnAction(e -> clearCanvas());
        sendDrawingButton.setDisable(true);
        setupCanvasEvents();
        fillWhiteBackground();
    }

    @FXML
    public void sendDrawing() {
        if (sendDrawingButton.isDisabled()) return;
        WritableImage snapshot = drawingCanvas.snapshot(null, null);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
            client.sendMessage("DRAWING:" + base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendDrawingButton.setDisable(true);
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    public void prepareNewRound() {
        clearCanvas();
        sendDrawingButton.setDisable(false);
        startTurnTimer();
    }

    private void setupCanvasEvents() {
        drawingCanvas.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.setLineWidth(sizeSlider.getValue());
            gc.setStroke(eraserToggle.isSelected() ? Color.WHITE : colorPicker.getValue());
        });

        drawingCanvas.setOnMouseDragged(e -> {
            if (currentTool == Tool.FREE && !isShapeToolSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        drawingCanvas.setOnMouseReleased(e -> {
            double endX = e.getX();
            double endY = e.getY();
            gc.setStroke(eraserToggle.isSelected() ? Color.WHITE : colorPicker.getValue());

            if (drawLine.isSelected()) {
                gc.strokeLine(startX, startY, endX, endY);
            } else if (drawRect.isSelected()) {
                gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
            } else if (drawOval.isSelected()) {
                gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY),
                        Math.abs(endX - startX), Math.abs(endY - startY));
            }
        });
    }

    private boolean isShapeToolSelected() {
        return drawLine.isSelected() || drawRect.isSelected() || drawOval.isSelected();
    }

    private void updateToggleStyles() {
        String activeStyle = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
        String defaultStyle = "-fx-font-size: 20px; -fx-font-weight: bold;";

        ToggleButton[] buttons = { drawLine, drawRect, drawOval, eraserToggle };

        boolean noneSelected = true;
        for (ToggleButton btn : buttons) {
            if (btn.isSelected()) {
                btn.setStyle(activeStyle);
                noneSelected = false;
            } else {
                btn.setStyle(defaultStyle);
            }
        }

        if (eraserToggle.isSelected()) {
            gc.setStroke(Color.WHITE);
        } else {
            gc.setStroke(colorPicker.getValue());
        }

        if (noneSelected) {
            currentTool = Tool.FREE;
            erasing = false;
        }
    }

    private void fillWhiteBackground() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.setStroke(colorPicker.getValue());
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        fillWhiteBackground();
    }

    public void startTurnTimer() {
        remainingSeconds = 60;
        timerLabel.setText("Czas: " + remainingSeconds + "s");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            timerLabel.setText("Czas: " + remainingSeconds + "s");

            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                sendDrawing(); // auto-wysyłka
            }
        }));

        countdownTimeline.setCycleCount(remainingSeconds);
        countdownTimeline.play();
    }

    public void setWord(String word) {
        wordLabel.setText("Twoje hasło: " + word);
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
