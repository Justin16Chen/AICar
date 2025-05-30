package aicar.utils.input;

public class Key extends InputBase {
    String name;
    public Key(String name) {
        this.name = name;
    }

    public boolean isAlphaNumeric() {
        return name.length() == 1 && Character.isLetterOrDigit(name.charAt(0));
    }
}