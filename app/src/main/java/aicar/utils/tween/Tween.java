package aicar.utils.tween;

import aicar.utils.math.JMath;

public class Tween extends Updatable {

    public static Tween createTween(String name, Object target, String propertyName, Number startValue, Number endValue, double duration) {
        Tween tween = new Tween(name, target, propertyName, startValue, endValue, duration, 0, 0, false);
        Updatables.addUpdatable(tween);
        return tween;
    }

    private Number startValue;      // Starting value of the tween
    private Number endValue;        // Ending value of the tween
    private double currentValue;
    private EaseType easeType;      // Type of easing to use

    private Tween(String name, Object target, String propertyName, Number startValue, Number endValue, double duration, int currentLoopCount, int targetLoopCount, boolean pingPong) {
        super(name, target, propertyName, duration, currentLoopCount, targetLoopCount, pingPong);
        this.startValue = startValue;
        this.endValue = endValue;
        this.easeType = new EaseType(Ease.LINEAR, 1);
        updateProperty(0);
    }

    public Number getStartValue() { return startValue; }
    public Number getEndValue() { return endValue;}

    public Tween setLoopCount(int loopCount) { targetLoopCount = loopCount; return this; }
    public Tween pingPong() { pingPong = true; pingPongDelay = -1; return this; }
    @Deprecated
    public Tween pingPong(double pingPongDelay) { pingPong = true; this.pingPongDelay = pingPongDelay; return this; }
    public Tween setPrint(PrintType print) { this.print = print; return this; }
    public Tween setEaseType(EaseType easeType) { this.easeType = easeType; return this; }

    @Override
    public String toString() {
        return "Tween(" + getName() + "|modifying " + getPropertyName() + "|current: " + currentValue+ "|complete:" + isComplete() + "|shouldDelete:" + shouldDelete() + ")";

    }

    public void update() {
        double t = Math.min(1, getElapsedTime() / getDuration());
        updateProperty(t);

        //if (name.equals("playerOutline"))
        //    System.out.println("target: " + getTarget());
    }

    // sets the property of the target object based on a normalized time (0 to 1)
    private void updateProperty(double t) {
        currentValue = JMath.lerp(startValue.doubleValue(), endValue.doubleValue(), easeType.calculate(t));
        Updatables.setProperty(getTarget(), getPropertyName(), currentValue);
    }

    @Override
    public void loop() {
        updateProperty(1);
        elapsedTime = 0;
        currentLoop++;
        if (pingPong) {
            if (targetLoopCount < 0)
                pingPong = true;
            else
                pingPong = currentLoop < targetLoopCount - 1;
            Number temp = startValue;
            startValue = endValue;
            endValue = temp;

            // set delay - DOES NOT WORK
            if (pingPongDelay > 0) {
                setPaused(true);
                System.out.println("PAUSING TIMER");
                Timer.createSetTimer("unpause " + getName(), this, pingPongDelay, "paused", false);
            }
        }
    }

    @Override
    public void performOnComplete() {
        updateProperty(1);
    }
}