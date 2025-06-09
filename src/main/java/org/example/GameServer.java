package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    private static final Map<String, List<GameRoom>> waitingRooms = new ConcurrentHashMap<>();
    private static final Map<String, GameRoom> activeRooms = new ConcurrentHashMap<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Queue<ClientHandler> handlers = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        try {
            NetworkConfigReader config = new NetworkConfigReader();
            int port = config.getPort();
            System.out.println("Serwer działa na porcie " + port);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    Player player = new Player("Anonim", socket, out);

                    ClientHandler handler = new ClientHandler(socket, player);
                    handlers.add(handler);
                    pool.execute(handler);
                    System.out.println("Nowy gracz połączony: " + "Anonim");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas uruchamiania serwera: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void handleGameRequest(Player player, String gameType) {
        handleGameRequest(player, gameType, 2); // MEMORY = 2 graczy domyślnie
    }

    public static void handleGameRequest(Player player, String gameType, int playerCount) {
        waitingRooms.putIfAbsent(gameType, new CopyOnWriteArrayList<>());
        List<GameRoom> rooms = waitingRooms.get(gameType);

        synchronized (rooms) {
            for (GameRoom room : rooms) {
                if (!room.isFull() && room.getMaxPlayers() == playerCount) {
                    room.addPlayer(player);

                    if (room.isFull()) {
                        for (Player p : room.getPlayers()) {
                            ReportWriter.logGameJoined(p.getName(), gameType);
                        }
                        room.getGame().startGame();
                        activeRooms.put(room.getId(), room);
                        rooms.remove(room);
                    }

                    return;
                }
            }

            // brak dostępnych pokoi – twórz nowy
            Game game = switch (gameType.toUpperCase()) {
                case "MEMORY" -> new MemoryGame();
                case "CHARADES" -> new CharadesGame(playerCount);
                default -> throw new IllegalArgumentException("Nieznany typ gry: " + gameType);
            };

            GameRoom newRoom = new GameRoom(game, playerCount);
            newRoom.addPlayer(player);
            rooms.add(newRoom);
        }
    }

    public static void forwardMove(Player player, String input) {
        for (GameRoom room : activeRooms.values()) {
            if (room.contains(player)) {
                room.getGame().handleMove(player, input);
                return;
            }
        }
    }
}