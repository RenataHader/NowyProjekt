package org.example;

public class Card {
    private final String value;
    private boolean flipped;
    private boolean matched;

    public Card(String value) {
        this.value = value;
        this.flipped = false;
        this.matched = false;
    }

    public String getValue() {
        return value;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}


