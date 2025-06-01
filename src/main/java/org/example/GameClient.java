package org.example;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {

    private static GameClient instance = null;

    public static synchronized GameClient getInstance()
    {
        if (instance == null)
        {
            instance = new GameClient();
        }

        return instance;
    }


    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessage;

    private CharadesGuessController guessController;
    private CharadesDrawController drawController;

    private GameClient() {
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread listener = new Thread(() -> {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    if (onMessage != null) {
                        onMessage.accept(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
        setOnMessage(msg -> this.messageHandler(msg));
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    public void setOnMessage(Consumer<String> handler) {
        this.onMessage = handler;
    }

    private void messageHandler(String message) {


        if(message.startsWith("START:CHARADES")){

        } else if (message.startsWith("START_GUESSING")) {
            Platform.runLater(() -> {
                ViewManager manager = ViewManager.getInstance();
                manager.changeView("charadesGuess");
            });
        } else if (message.startsWith("DRAWING_FROM:")) {
            Platform.runLater(() -> {
                ViewManager manager = ViewManager.getInstance();
                guessController = manager.getController("charadesGuess", CharadesGuessController.class);
                guessController.processDrawing(message);
            });
        }
    }
}
