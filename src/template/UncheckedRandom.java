package template;

public class UncheckedRandom {
    static long x = (long) (Math.random() * 0xffffffffL) & 0xffffffffL;
    public static int uncheckedNextInt() {
        x = (0xffffda61L * (x & 0xffffffffL)) + (x >>> 32);
        return (int) x;
    }
    public static int uncheckedNextInt(int bound) {
        return uncheckedNextInt() % bound;
    }
}
