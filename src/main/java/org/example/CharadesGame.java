package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CharadesGame extends Game {

    private String[] drawings;
    private boolean[] hasGuessed;
    private final int expectedPlayerCount;
    private final Map<String, Boolean> guessedMap = new HashMap<>();
    private int drawingsSubmitted = 0;
    private int currentRound = 1;
    private int totalRounds  = 4;
    private Map<Player, List<String>> playerWords = new HashMap<>();

    public CharadesGame(int expectedPlayerCount) {
        this.expectedPlayerCount = expectedPlayerCount;
        this.drawings = new String[expectedPlayerCount];
        this.hasGuessed = new boolean[expectedPlayerCount];
    }

    @Override
    public void startGame() {
        assignWordsToPlayers();
        for (Player p : players) {
            p.resetScore();
        }
        sendNicknames();
        sendScoresToAll();
        sendWordsForCurrentRound();
        broadcast("DRAWING_TO:");
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
        broadcast("Gra w Kalambury zakończona!");

        resetGameState();

        drawingsSubmitted = 0;
        Arrays.fill(drawings, null);
        guessedMap.clear();
    }

    private void processSentDrawing(int playerId, String base64) {
        drawings[playerId] = base64;
        drawingsSubmitted++;

        Player sender = players.get(playerId);
        System.out.println("Otrzymano rysunek od " + sender.getName());

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
        if (guessedMap.containsKey(key)) return;

        guessedMap.put(key, true);

        Player target = players.stream()
                .filter(p -> p.getName().equals(targetSenderName))
                .findFirst()
                .orElse(null);

        if (target == null) return;

        List<String> words = playerWords.get(target);
        String expectedWord = (words != null && currentRound - 1 < words.size()) ? words.get(currentRound - 1) : null;

        if (expectedWord != null && isCorrectGuess(expectedWord, guessText)) {
            guesser.addScore(1);
            target.addScore(1);

            guesser.sendMessage("RESULT: Poprawnie! Hasło gracza " + targetSenderName + ": " + expectedWord);
            sendScoresToAll();
        } else {
            guesser.sendMessage("RESULT: Niepoprawnie! Spróbuj dalej.");
        }

        int totalGuessesNeeded = expectedPlayerCount * (expectedPlayerCount - 1);
        if (guessedMap.size() >= totalGuessesNeeded) {
            if (currentRound < totalRounds) {
                resetGameState();
                currentRound++;
                sendWordsForCurrentRound();
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

    private void assignWordsToPlayers() {
        try {
            List<String> allWords = Files.readAllLines(Paths.get("src/main/resources/words.txt"));
            Collections.shuffle(allWords);
            int totalWordsNeeded = expectedPlayerCount * totalRounds;

            if (allWords.size() < totalWordsNeeded) {
                throw new RuntimeException("Za mało słów w bazie!");
            }

            int index = 0;
            for (Player player : players) {
                List<String> wordsForPlayer = new ArrayList<>();
                for (int i = 0; i < totalRounds; i++) {
                    wordsForPlayer.add(allWords.get(index++));
                }
                playerWords.put(player, wordsForPlayer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWordsForCurrentRound() {
        for (Player player : players) {
            List<String> words = playerWords.get(player);
            if (words != null && currentRound - 1 < words.size()) {
                String word = words.get(currentRound - 1);
                player.sendMessage("WORD:" + word);
            }
        }
    }

    private boolean isCorrectGuess(String expected, String actual) {
        return expected.trim().equalsIgnoreCase(actual.trim());
    }

    private void sendScoresToAll() {
        StringBuilder sb = new StringBuilder("SCORES:");
        for (Player p : players) {
            sb.append(p.getName()).append("=").append(p.getScore()).append(",");
        }
        broadcast(sb.toString());
    }

    private void sendNicknames() {
        StringBuilder sb = new StringBuilder("NICKICH:");
        for (Player p : players) {
            sb.append(p.getName()).append(",");
        }
        broadcast(sb.toString());
    }

}
