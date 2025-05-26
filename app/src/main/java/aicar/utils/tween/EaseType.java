package aicar.utils.tween;

public class EaseType {

    private static double getEasingFunction(EaseType easeType, double x) {
        double c1 = 1.70158, c3 = c1 + 1, c2 = c1 * 1.525;;
        switch (easeType.easeOption) {
            case LINEAR:
                return x;
            case EASE_IN:
                return Math.pow(x, easeType.easePower);
            case EASE_OUT:
                return 1 - Math.pow(1 - x, easeType.easePower);
            case EASE_IN_OUT:
                return x < 0.5 ? 4 * Math.pow(x, easeType.easePower) : 1 - Math.pow(-2 * x + 2, easeType.easePower) / 2;
            case EASE_IN_BACK:
                return c3 * Math.pow(x, easeType.easePower) - c1 * Math.pow(x, easeType.easePower - 1);
            case EASE_OUT_BACK:
                return 1 + c3 * Math.pow(x - 1, easeType.easePower) + c1 * Math.pow(x - 1, easeType.easePower - 1);
            case EASE_IN_OUT_BACK:
                return x < 0.5
                    ? (Math.pow(2 * x, easeType.easePower - 1) * ((c2 + 1) * 2 * x - c2)) / 2
                    : (Math.pow(2 * x - 2, easeType.easePower - 1) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
            default:
                return x;
        }
    }
    private static double getDefaultEasePower(Ease easeOption) {
        switch (easeOption) {
            case LINEAR: return 1;
            case EASE_IN:
            case EASE_OUT:
            case EASE_IN_OUT:
            case EASE_IN_BACK:
            case EASE_OUT_BACK:
            case EASE_IN_OUT_BACK: return 3;
            default: return 1;
        }
    }
    
    private Ease easeOption;
    private double easePower;

    public EaseType(Ease easeOption) {
        this.easeOption = easeOption;
        easePower = getDefaultEasePower(easeOption);
    }
    public EaseType(Ease easeOption, double easePower) {
        this.easeOption = easeOption;
        this.easePower = easePower;
    }

    public Ease getEase() { return easeOption; }
    public double getEasePower() { return easePower; }
    public double calculate(double x) {
        return getEasingFunction(this, x);
    }
}