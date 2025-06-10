package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;

public class ClientHandler implements Callable<Void> {

    private final Socket socket;
    private final Player player;

    public ClientHandler(Socket socket, Player player) {
        this.socket = socket;
        this.player = player;
    }

    @Override
    public Void call() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String input;
            while ((input = in.readLine()) != null) {

                if (input.startsWith("NICK:")) {
                    String nick = input.substring(5).trim();
                    if (!nick.isEmpty()) {
                        player.setName(nick);
                        ReportWriter.logPlayerCreated(nick);
                        System.out.println("Gracz ustawił nick: " + nick);
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

        } catch (SocketException e) {
            System.out.println("Gracz '" + player.getName() + "' rozlaczyl sie.");
        } catch (Exception e) {
            System.err.println("Blad podczas obsługi gracza: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception ex) {
                System.err.println("Blad przy zamykaniu socketu: " + ex.getMessage());
            }
        }

        return null;
    }
}
