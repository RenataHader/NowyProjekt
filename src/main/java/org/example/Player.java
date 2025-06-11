package org.example;

import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private String name;
    private final Socket socket;
    private final PrintWriter out;
    private int id;


    private int score = 0;

    public Player(String name, Socket socket, PrintWriter out) {
        this.name = name;
        this.socket = socket;
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return out;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void resetScore() {
        score = 0;
    }
}
