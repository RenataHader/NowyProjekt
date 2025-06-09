package org.example;

public interface GameGUIController {
    void initialize();
    void setNick(String msg);
    void setScore(String msg);
    void startTurnTimer();
    void updateTurn(String playerName);
}
