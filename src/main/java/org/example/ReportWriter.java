package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportWriter {

    private static final String FILE_NAME = "raport.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logPlayerCreated(String playerName) {
        writeLine("Utworzono gracza: " + playerName);
    }

    public static void logGameJoined(String playerName, String gameType) {
        writeLine("Gracz " + playerName + " dołączył do gry: " + gameType);
    }

    public static void logGameResult(String winner, String playersCsv) {
        writeLine("Wynik gry — zwycięzca: " + winner + ", gracze: " + playersCsv);
    }

    private static void writeLine(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(LocalDateTime.now().format(FORMATTER) + " " + line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}