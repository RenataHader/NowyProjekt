    package org.example;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;

    public class GameRoom {
        private final String id = UUID.randomUUID().toString();
        private final Game game;
        private final List<Player> players = new ArrayList<>();

        private final int maxPlayers;

        public GameRoom(Game game, int maxPlayers) {
            this.game = game;
            this.maxPlayers = maxPlayers;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void addPlayer(Player player) {
            players.add(player);
            game.addPlayer(player);
        }

        public boolean isFull() {
            return players.size() >= maxPlayers;
        }

        public boolean contains(Player player) {
            return players.contains(player);
        }

        public Game getGame() {
            return game;
        }

        public String getId() {
            return id;
        }

        public List<Player> getPlayers() {
            return players;
        }
    }
