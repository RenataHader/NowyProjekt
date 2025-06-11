package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Game {
    protected int gameDatabaseId;
    protected final List<Player> players = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void setGameDatabaseId(int id) {
        this.gameDatabaseId = id;
    }

    public int getGameDatabaseId() {
        return gameDatabaseId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract void handleMove(Player player, String moveData);
}
