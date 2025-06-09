package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NetworkConfigReader {
    private String ip;
    private int port;

    public NetworkConfigReader() {
        readConfig();
    }

    private void readConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader("network_config.txt"))) {
            ip = reader.readLine();
            port = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            System.err.println("Error reading network config file, using defaults");
            ip = "localhost";
            port = 12345;
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}