package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Player player;

    public ClientHandler(Socket socket, Player player) {
        this.socket = socket;
        this.player = player;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String input;
            while ((input = in.readLine()) != null) {

                if (input.startsWith("NICK:")) {
                    String nick = input.substring(5).trim();
                    if (!nick.isEmpty()) {
                        player.setName(nick);
                        ReportWriter.logPlayerCreated(nick);
                        System.out.println("✔️ Gracz ustawił nick: " + nick);
                    }
                    continue;
                }

                if (input.startsWith("GAME:")) {
                    String[] parts = input.split(":");

                    String gameType = parts[1].toUpperCase();

                    if ("CHARADES".equals(gameType) && parts.length == 3) {
                        int playerCount = Integer.parseInt(parts[2]);
                        GameServer.handleGameRequest(player, gameType, playerCount);
                    } else {
                        GameServer.handleGameRequest(player, gameType);
                    }

                } else {
                    GameServer.forwardMove(player, input);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}