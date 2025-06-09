package org.example;

import javafx.application.Platform;

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
    private MemoryController memoryController;

    private GameClient() {
    }

    public void connect(String host, int port) throws IOException {
        if (socket != null && socket.isConnected()) return;
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

        ViewManager manager = ViewManager.getInstance();
        if(message.startsWith("START:CHARADES")){

        } else if (message.startsWith("START_GUESSING")) {
            Platform.runLater(() -> {
                manager.changeView("charadesGuess");
                guessController = manager.getController("charadesGuess", CharadesGuessController.class);
                guessController.prepareNewRound();
            });
        } else if (message.startsWith("DRAWING_FROM:")) {
            Platform.runLater(() -> {
                guessController = manager.getController("charadesGuess", CharadesGuessController.class);
                guessController.processShowPicture(message);
            });
        } else if (message.startsWith("DRAWING_TO:")) {
            Platform.runLater(() -> {
                manager.changeView("charadesDraw");
                drawController = manager.getController("charadesDraw", CharadesDrawController.class);
                drawController.prepareNewRound();
            });
        } else if (message.startsWith("UPDATE:")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.turnCard(message);
            });
        } else if (message.startsWith("MATCH:")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.matchCard(message);
            });
        } else if (message.startsWith("HIDE:")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.backCard(message);
            });
        } else if (message.startsWith("Server: Wynik - ")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.setScore(message);
            });
        } else if (message.startsWith("Server:")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.setServer(message);
            });
        } else if (message.startsWith("NICKI:")) {
            Platform.runLater(() -> {
                memoryController = manager.getController("memory", MemoryController.class);
                memoryController.setNick(message);
            });
        }
    }
}
