package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Game {
    protected final String gameId = UUID.randomUUID().toString();
    protected final List<Player> players = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
    }

    public String getGameId() {
        return gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract void handleMove(Player player, String moveData);
}
