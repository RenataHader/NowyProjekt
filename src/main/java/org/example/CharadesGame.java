package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CharadesGame extends Game {

    private String[] drawings;
    private boolean[] hasGuessed;
    private final int expectedPlayerCount;
    private final Map<String, Boolean> guessedMap = new HashMap<>();
    private int drawingsSubmitted = 0;
    private int currentRound = 1;
    private int totalRounds  = 4;

    public CharadesGame(int expectedPlayerCount) {
        this.expectedPlayerCount = expectedPlayerCount;
        this.drawings = new String[expectedPlayerCount];
        this.hasGuessed = new boolean[expectedPlayerCount];
    }

    @Override
    public void startGame() {
        broadcast("START:CHARADES");
    }

    @Override
    public void handleMove(Player player, String message) {
        int playerId = players.indexOf(player);
        if (playerId < 0) return;

        if (message.startsWith("DRAWING:")) {
            processSentDrawing(playerId, message.substring(8));
            System.out.println("Rysunek od gracza: " + player.getName());

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

        resetGameState();

        drawingsSubmitted = 0;
        Arrays.fill(drawings, null);
        guessedMap.clear();
    }

    private void processSentDrawing(int playerId, String base64) {
        drawings[playerId] = base64;
        drawingsSubmitted++;

        Player sender = players.get(playerId);
        System.out.println("✔️ Otrzymano rysunek od " + sender.getName());

        if (drawingsSubmitted == expectedPlayerCount) {
            broadcast("START_GUESSING");

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

        int totalGuessesNeeded = expectedPlayerCount * (expectedPlayerCount - 1);

        if (guessedMap.size() >= totalGuessesNeeded) {
            if (currentRound < totalRounds) {
                broadcast("Server: Wszyscy gracze zakończyli zgadywanie w tej turze!");
                resetGameState();
                currentRound++;
                broadcast("DRAWING_TO:");
            } else {
                endGame();
            }
        }

    }
    private void resetGameState() {
        for (int i = 0; i < expectedPlayerCount; i++) {
            drawings[i] = null;
            hasGuessed[i] = false;
        }
        drawingsSubmitted = 0;
        guessedMap.clear();
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
