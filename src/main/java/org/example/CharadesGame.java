package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CharadesGame extends Game {

    private String[] drawings;
    private boolean[] hasGuessed;
    private final int expectedPlayerCount;
    private final Map<String, Boolean> guessedMap = new HashMap<>();

    public CharadesGame(int expectedPlayerCount) {
        this.expectedPlayerCount = expectedPlayerCount;
        this.drawings = new String[expectedPlayerCount];
        this.hasGuessed = new boolean[expectedPlayerCount];
    }

    @Override
    public void startGame() {
        broadcast("START:CHARADES");
        broadcast("Server: Tryb Kalambury rozpoczęty!");
    }

    @Override
    public void handleMove(Player player, String message) {
        int playerId = players.indexOf(player);
        if (playerId < 0) return;

        if (message.startsWith("DRAWING:")) {
            processDrawing(playerId, message.substring(8));
            System.out.println("Rysunek od gracza: " + player.getName());

            if (Arrays.stream(drawings).allMatch(Objects::nonNull)) {
                for (int i = 0; i < players.size(); i++) {
                    Player receiver = players.get(i);
                    for (int j = 0; j < players.size(); j++) {
                        if (i != j) {
                            String senderName = players.get(j).getName();
                            System.out.println("⮕ Do gracza " + receiver.getName() + " wysyłam rysunek od " + senderName);
                            receiver.sendMessage("DRAWING_FROM:" + senderName + ":" + drawings[j]);
                        }
                    }
                }

                for (Player p : players) {
                    p.sendMessage("START_GUESSING");
                }
            }

        } else if (message.startsWith("GUESS:")) {
            String[] parts = message.substring(6).split(":", 2);
            if (parts.length == 2) {
                processGuess(player, parts[0], parts[1]);
            }
        }
    }

    @Override
    public void endGame() {
        StringBuilder summary = new StringBuilder("Gra zakończona. Gracze: ");
        for (Player p : players) {
            summary.append(p.getName()).append(" ");
        }

        ReportWriter.logGameResult("Wszyscy zakończyli zgadywanie", summary.toString(), "");
        broadcast("Server: Gra w Kalambury zakończona!");
    }

    private void processDrawing(int playerId, String base64) {
        drawings[playerId] = base64;
        Player sender = players.get(playerId);
        System.out.println("✔️ Otrzymano rysunek od " + sender.getName());

        if (allDrawingsSubmitted()) {
            broadcast("Server: Wszystkie rysunki zebrane. Czas na zgadywanie!");

            // Każdy gracz otrzymuje rysunki pozostałych graczy
            for (int i = 0; i < players.size(); i++) {
                Player receiver = players.get(i);
                for (int j = 0; j < players.size(); j++) {
                    if (i != j && drawings[j] != null) {
                        String senderName = players.get(j).getName();
                        receiver.sendMessage("DRAWING_FROM:" + senderName + ":" + drawings[j]);
                        System.out.println("➜ Wysyłam rysunek " + senderName + " do " + receiver.getName());
                    }
                }
            }
        }
    }

    private void processGuess(Player guesser, String targetSenderName, String guessText) {
        String key = guesser.getName() + "->" + targetSenderName;
        if (!guessedMap.containsKey(key)) {
            guessedMap.put(key, true);
            guesser.sendMessage("RESULT: Zgadłeś rysunek " + targetSenderName + ": " + guessText);
        }

        if (guessedMap.size() >= expectedPlayerCount * (expectedPlayerCount - 1)) {
            broadcast("Server: Wszyscy gracze zakończyli zgadywanie!");
            endGame();
        }
    }

    private boolean allDrawingsSubmitted() {
        for (String d : drawings) {
            if (d == null) return false;
        }
        return true;
    }

    private void resetGameState() {
        for (int i = 0; i < expectedPlayerCount; i++) {
            drawings[i] = null;
            hasGuessed[i] = false;
        }
    }

    private void broadcast(String msg) {
        for (Player p : players) {
            p.sendMessage(msg);
        }
    }

    public int getExpectedPlayerCount() {
        return expectedPlayerCount;
    }

}
