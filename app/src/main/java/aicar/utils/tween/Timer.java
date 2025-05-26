package aicar.utils.tween;

public class Timer extends Updatable {

    public static Timer createSetTimer(String name, Object target, double duration, String propertyName, Object finalPropertyValue) {
        Timer timer = new Timer(name, target, duration, propertyName, finalPropertyValue, 0, 0);
        Updatables.addUpdatable(timer);
        return timer;
    }
    public static Timer createCallTimer(String name, Object target, double duration, String methodName, Object... args) {
        Timer timer = new Timer(name, target, duration, methodName, 0, 0, args);
        Updatables.addUpdatable(timer);
        return timer;
    }
    public static Timer resetSetTimerTo(String name, Object target, double duration, String propertyName, Object finalPropertyValue, int currentLoop, int targetLoopCount) {
        Timer timer = new Timer(name, target, duration, propertyName, finalPropertyValue, currentLoop, targetLoopCount);
        Updatables.addUpdatable(timer);
        return timer;
    }
    public static Timer resetCallTimerTo(String name, Object target, double duration, String methodName, int currentLoop, int targetLoopCount, Object... args) {
        Timer timer = new Timer(name, target, duration, methodName, currentLoop, targetLoopCount, args);
        Updatables.addUpdatable(timer);
        return timer;
    }

    private Object finalPropertyValue;

    public Timer(String name, Object target, double duration, String propertyName, Object finalPropertyValue, int currentLoopCount, int targetLoopCount) {
        super(name, target, propertyName, duration, currentLoopCount, targetLoopCount, false);
        this.finalPropertyValue = finalPropertyValue;
    }

    public Timer(String name, Object target, double duration, String methodName, int currentLoopCount, int targetLoopCount, Object... args) {
        super(name, target, methodName, duration, currentLoopCount, targetLoopCount, false, args);
    }

    public Timer setLoopCount(int loopCount) { targetLoopCount = loopCount; return this; }
    public Timer setPrint(PrintType print) { this.print = print; return this; }

    @Override
    public String toString() {
        if (getType() == Type.SET) {
            return "Timer(name: " + getName() + " | set property: " + getPropertyName() + " to " + finalPropertyValue + " | duration: " + getDuration() + " | elapsed time: " + getElapsedTime() + ")";
        }
        return "Timer(name: " + getName() + " | call function " + getMethodName() + " with " + Updatables.getParameterTypesString(getMethodArgs()) + " | elapsed time: " + getElapsedTime() + "/" + getDuration() + " | loop complete: " + isLoopComplete() + " | complete: " + isComplete() + ")";
    }

    public Object getFinalPropertyValue() { return finalPropertyValue; }

    // timer doesn't need to do anything special while it is waiting
    @Override
    public void update() {}

    
    @Override
    public void loop() {
        elapsedTime = 0;
        currentLoop++;
    }

    @Override
    public void performOnLoopComplete() {
        if (getType() == Type.SET) 
            Updatables.setProperty(getTarget(), getPropertyName(), finalPropertyValue);
        else if (getType() == Type.CALL) 
            Updatables.callMethodByName(getTarget(), getMethodName(), getMethodArgs());
    }
}
