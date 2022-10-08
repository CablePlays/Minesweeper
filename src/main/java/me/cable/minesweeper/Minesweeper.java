package me.cable.minesweeper;

import me.cable.minesweeper.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Minesweeper {

    private final int gridSize;
    private final double bombChance;
    private final Tile[][] tiles;
    private int remainingTiles;
    private boolean gameOver;

    public Minesweeper(int gridSize, double bombChance) {
        this.gridSize = gridSize;
        this.bombChance = bombChance;
        tiles = new Tile[gridSize][gridSize];
        createTiles();
    }

    public void reset() {
        gameOver = false;
        createTiles();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                makeDefault(x, y);
            }
        }
    }

    private void createTiles() {
        remainingTiles = gridSize * gridSize;

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                boolean bomb = Utils.chance(bombChance);
                Tile tile = new Tile(x, y, bomb);
                tiles[x][y] = tile;

                if (bomb) {
                    remainingTiles--;
                }
            }
        }
    }

    public final void hint() {
        if (gameOver) {
            return;
        }

        List<Tile> available = new ArrayList<>();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Tile tile = getTile(x, y);

                if (!tile.isDiscovered() && !tile.isBomb() && getNearbyBombs(tile) == 0) {
                    available.add(tile);
                }
            }
        }

        int size = available.size();

        if (size == 0) {
            throw new IllegalStateException("No Zeros");
        }

        Tile tile = available.get((int) (Math.random() * size));
        tile.setFlagged(false);
        trigger(tile.getX(), tile.getY());
    }

    /*
        Must be called from external program.
     */
    public final void trigger(int x, int y) {
        if (gameOver) {
            return;
        }

        Tile tile = getTile(x, y);
        if (tile.isDiscovered() || tile.isFlagged()) return;

        if (tile.isBomb()) {
            gameLose(x, y);
            gameOver = true;

            for (int xa = 0; xa < gridSize; xa++) {
                for (int ya = 0; ya < gridSize; ya++) {
                    if (getTile(xa, ya).isBomb() && (xa != x || ya != y)) {
                        makeBomb(xa, ya);
                    }
                }
            }
        } else {
            tile.setDiscovered(true);
            int nearbyBombs = getNearbyBombs(tile);

            if (--remainingTiles <= 0 || nearbyBombs == 0 && uncoverAdjacentZeros(tile)) {
                gameWin(x, y, nearbyBombs);
                gameOver = true;

                for (int xa = 0; xa < gridSize; xa++) {
                    for (int ya = 0; ya < gridSize; ya++) {
                        if (getTile(xa, ya).isBomb()) {
                            makeFlag(xa, ya);
                        }
                    }
                }
            } else {
                makeUncovered(x, y, nearbyBombs);
            }
        }
    }

    /*
        Returns true if there are no more remaining tiles.
     */
    private boolean uncoverAdjacentZeros(@NotNull Tile tile) {
        for (Tile near : getNearbyTiles(tile)) {
            if (near.isDiscovered() || near.isFlagged()) {
                continue;
            }

            int nearbyBombs = getNearbyBombs(near);
            near.setDiscovered(true);
            makeUncovered(near.getX(), near.getY(), nearbyBombs);

            if (--remainingTiles <= 0 || nearbyBombs == 0 && uncoverAdjacentZeros(near)) {
                return true;
            }
        }

        return false;
    }

    /*
        Must be called from external program.
     */
    public final void toggle(int x, int y) {
        if (gameOver) {
            return;
        }

        Tile tile = getTile(x, y);
        if (tile.isDiscovered()) return;

        boolean newFlagState = !tile.isFlagged();
        tile.setFlagged(newFlagState);

        if (newFlagState) {
            makeFlag(x, y);
        } else {
            makeDefault(x, y);
        }
    }

    protected abstract void makeDefault(int x, int y);

    protected abstract void makeUncovered(int x, int y, int n);

    protected abstract void makeFlag(int x, int y);

    protected abstract void makeBomb(int x, int y);

    protected abstract void gameLose(int x, int y);

    protected abstract void gameWin(int x, int y, int n);

    private @NotNull Tile getTile(int x, int y) {
        if (!Utils.inRange(0, gridSize - 1, x, y)) {
            throw new IllegalArgumentException("Invalid x and y: " + x + ", " + y);
        }

        return tiles[x][y];
    }

    /*
        Returns a list of the surrounding tiles.
     */
    private @NotNull List<Tile> getNearbyTiles(@NotNull Tile tile) {
        List<Tile> list = new ArrayList<>();
        int x = tile.getX();
        int y = tile.getY();

        for (int xa = x - 1; xa <= x + 1; xa++) {
            for (int ya = y - 1; ya <= y + 1; ya++) {
                if (Utils.inRange(0, gridSize - 1, xa, ya) && (xa != x || ya != y)) {
                    list.add(getTile(xa, ya));
                }
            }
        }

        return list;
    }

    private int getNearbyBombs(@NotNull Tile tile) {
        int n = 0;

        for (Tile t : getNearbyTiles(tile)) {
            if (t.isBomb()) {
                n++;
            }
        }

        return n;
    }
}
