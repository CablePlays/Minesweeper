package me.cable.minesweeper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class Minesweeper {

    public final int gridSize;
    public final double bombChance;

    private final Tile[][] tiles;
    private int remainingTiles;
    private boolean gameOver;

    public Minesweeper(int gridSize, double bombChance) {
        tiles = new Tile[gridSize][gridSize];
        this.gridSize = gridSize;
        this.bombChance = bombChance;
        createTiles();
    }

    public void reset() {
        gameOver = false;
        createTiles();

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Tile tile = getTile(x, y);
                changeTile(tile, TileState.COVERED);
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
            gameOver = true;
            changeTile(tile, TileState.LOSE);
            onLose();

            for (int xa = 0; xa < gridSize; xa++) {
                for (int ya = 0; ya < gridSize; ya++) {
                    Tile current = getTile(xa, ya);

                    if (current.isBomb() && (xa != x || ya != y)) {
                        changeTile(current, TileState.BOMB);
                    }
                }
            }
        } else {
            tile.setDiscovered(true);
            int nearbyBombs = getNearbyBombs(tile);

            if (--remainingTiles <= 0 || nearbyBombs == 0 && uncoverAdjacentZeros(tile)) {
                gameOver = true;
                changeTile(tile, TileState.WIN);
                onWin();

                for (int xa = 0; xa < gridSize; xa++) {
                    for (int ya = 0; ya < gridSize; ya++) {
                        Tile current = getTile(xa, ya);

                        if (current.isBomb()) {
                            changeTile(current, TileState.FLAGGED);
                        }
                    }
                }
            } else {
                changeTile(tile, TileState.UNCOVERED);
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
            changeTile(near, TileState.UNCOVERED);

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

        boolean nowFlagged = !tile.isFlagged();
        tile.setFlagged(nowFlagged);

        if (nowFlagged) {
            changeTile(tile, TileState.FLAGGED);
        } else {
            changeTile(tile, TileState.COVERED);
        }
    }

    private void changeTile(@NotNull Tile tile, @NotNull TileState tileState) {
        changeTile(tile.getX(), tile.getY(), getNearbyBombs(tile), tileState);
    }

    public abstract void changeTile(int x, int y, int nearbyBombs, @NotNull TileState tileState);

    protected void onLose() {

    }

    protected void onWin() {

    }

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
