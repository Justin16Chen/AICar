package aicar.utils.misc;

public class Print {
    public static final String RESET = "\033[0m";  // Reset to default color
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE = "\033[0;37m";

    public static void println(String string) {
        System.out.println(string);
    }
    public static void println(String string, String color) {
        System.out.println(color + string + RESET);
    }
    public static void print(String string) {
        System.out.print(string);
    }
    public static void print(String string, String color) {
        System.out.print(color + string + RESET);
    }
}