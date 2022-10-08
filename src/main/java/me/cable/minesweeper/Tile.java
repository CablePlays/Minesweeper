package me.cable.minesweeper;

import org.jetbrains.annotations.NotNull;

public class Tile {

    private final int x;
    private final int y;
    private final boolean bomb;
    private boolean discovered;
    private boolean flagged;

    public Tile(int x, int y, boolean bomb) {
        this.x = x;
        this.y = y;
        this.bomb = bomb;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public @NotNull String getXY() {
        return x + "," + y;
    }

    public boolean isBomb() {
        return bomb;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}
