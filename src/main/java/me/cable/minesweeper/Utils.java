package me.cable.minesweeper;

public final class Utils {

    public static boolean chance(double chance) {
        return Math.random() * 100 < chance;
    }

    public static boolean inRange(int min, int max, int... numbers) {
        for (int n : numbers) {
            if (n < min || n > max) {
                return false;
            }
        }

        return true;
    }
}
