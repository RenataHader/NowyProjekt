package org.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class CharadesDrawController {

    @FXML private Canvas drawingCanvas;
    @FXML private Button sendDrawingButton;

    private GameClient client = GameClient.getInstance();
    private GraphicsContext gc;

    @FXML
    public void initialize() {
        gc = drawingCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);

        drawingCanvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });

        drawingCanvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });
    }

    @FXML
    public void sendDrawing() {
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
    }

    public void prepareNewRound() {
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        sendDrawingButton.setDisable(false);
    }
}
