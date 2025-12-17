package aicar.utils.misc;

// allows any class to statically access screen dimensions
public class ScreenSize {
    private static int width, height;
    public static void setScreenSize(int width, int height) {
        ScreenSize.width = width;
        ScreenSize.height = height;
    }
    public static int getWidth() {
        return width;
    }
    public static int getHeight() {
        return height;
    }
}
