package org.example;

import java.util.*;

public class MemoryGame extends Game {

    private final Card[][] board = new Card[3][8];
    private final List<String> flippedPositions = new ArrayList<>();
    private int currentPlayer = 0;

    @Override
    public void startGame() {
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            values.add(String.valueOf(i));
            values.add(String.valueOf(i));
        }
        Collections.shuffle(values);

        int index = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (index < values.size()) {
                    board[i][j] = new Card(values.get(index++));
                } else {
                    board[i][j] = new Card(" ");
                }
            }
        }

        for (Player p : players) {
            p.resetScore();
        }

        broadcast("START:MEMORY");
        String nick1 = players.get(0).getName();
        String nick2 = players.get(1).getName();
        broadcast("NICKI:" + nick1 + "," + nick2);
        broadcast("Server: Tura gracza " + nick1);
    }

    @Override
    public void handleMove(Player player, String moveData) {
        int playerId = players.indexOf(player);
        if (playerId != currentPlayer) return;

        String[] coords = moveData.split(",");
        if (coords.length != 2) return;

        int r = Integer.parseInt(coords[0]);
        int c = Integer.parseInt(coords[1]);
        Card selected = board[r][c];

        if (selected.isFlipped() || selected.isMatched()) return;

        selected.setFlipped(true);
        flippedPositions.add(r + "," + c);
        broadcast("UPDATE:" + r + "," + c + "=" + selected.getValue());

        if (flippedPositions.size() == 2) {
            String[] pos1 = flippedPositions.get(0).split(",");
            String[] pos2 = flippedPositions.get(1).split(",");

            Card card1 = board[Integer.parseInt(pos1[0])][Integer.parseInt(pos1[1])];
            Card card2 = board[Integer.parseInt(pos2[0])][Integer.parseInt(pos2[1])];

            if (card1.getValue().equals(card2.getValue())) {
                card1.setMatched(true);
                card2.setMatched(true);
                player.addScore(1);
                broadcast("MATCH:" + flippedPositions.get(0) + "|" + flippedPositions.get(1));
                broadcast("Server: Wynik - " +
                        players.get(0).getName() + ": " + players.get(0).getScore() + " " +
                        players.get(1).getName() + ": " + players.get(1).getScore());

                if (isGameFinished()) {
                    endGame();
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                card1.setFlipped(false);
                card2.setFlipped(false);
                broadcast("HIDE:" + flippedPositions.get(0) + "|" + flippedPositions.get(1));
                currentPlayer = 1 - currentPlayer;
                Player current = players.get(currentPlayer);
                broadcast("Server: Tura gracza " + current.getName());
            }
            flippedPositions.clear();
        }
    }

    @Override
    public void endGame() {
        Player p1 = players.get(0);
        Player p2 = players.get(1);
        String winnerName;

        if (p1.getScore() > p2.getScore()) {
            winnerName = p1.getName();
        } else if (p2.getScore() > p1.getScore()) {
            winnerName = p2.getName();
        } else {
            winnerName = "Remis";
        }
        ReportWriter.logGameResult(winnerName,p1.getName(), p2.getName());
        broadcast("Server: Gra zakończona! Zwycięzca: " + winnerName);
    }

    private boolean isGameFinished() {
        for (Card[] row : board) {
            for (Card card : row) {
                if (!card.isMatched()) return false;
            }
        }
        return true;
    }

    private void broadcast(String message) {
        for (Player p : players) {
            p.sendMessage(message);
        }
    }
}