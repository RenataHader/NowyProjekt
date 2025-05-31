package org.example;

import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private String name;
    private final Socket socket;
    private final PrintWriter out;

    public Player(String name, Socket socket, PrintWriter out) {
        this.name = name;
        this.socket = socket;
        this.out = out;
    }

    public String getName() {
        return name;
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
}
