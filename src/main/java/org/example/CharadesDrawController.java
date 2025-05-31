package org.example;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class CharadesDrawController {

    @FXML private Canvas drawingCanvas;
    @FXML private Button sendDrawingButton;

    private GameClient client;
    private GraphicsContext gc;

    public void setClient(GameClient client) {
        this.client = client;

        client.setOnMessage(msg -> {
            if (msg.startsWith("START_GUESSING")) {
                Platform.runLater(() -> switchToGuessingView());
            }
        });
    }

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

    private void switchToGuessingView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CharadesGuess.fxml"));
            Parent root = loader.load();

            CharadesGuessController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) drawingCanvas.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
