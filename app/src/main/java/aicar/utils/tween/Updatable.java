package aicar.utils.tween;

public abstract class Updatable {

    public enum PrintType {
        ALWAYS,
        NEVER,
        ON_COMPLETE,
        ON_LOOP
    }
    
    public enum Type {
        SET, CALL
    }
    protected String name;
    protected Type type;
    protected Object targetObject;         // The object to be updatable
    protected String propertyName;   // The name of the property to modify
    protected String methodName;
    protected Object[] methodArgs;
    protected double duration;        // Duration of the updatable
    protected double elapsedTime;     // Time elapsed since the updatable started
    protected int currentLoop;        // Current loop count
    protected int targetLoopCount;          // Number of times to loop the updatable
    protected boolean pingPong;
    @Deprecated
    protected double pingPongDelay;
    protected boolean paused;         // if the updatable is paused or not
    protected PrintType print;          // Print values for debugging
    private boolean delete;

    public Updatable(String name, Object targetObject, String propertyName, double duration, int currentLoopCount, int targetLoopCount, boolean pingPong) {
        this.name = name;
        this.type = Type.SET;
        this.targetObject = targetObject;
        this.propertyName = propertyName;
        this.duration = duration;
        this.currentLoop = currentLoopCount;
        this.targetLoopCount = targetLoopCount;
        this.pingPong = pingPong;
        this.elapsedTime = 0;
        this.print = PrintType.NEVER;
    }

    public Updatable(String name, Object targetObject, String methodName, double duration, int currentLoopCount, int targetLoopCount, boolean pingPong, Object...methodArgs) {
        this.name = name;
        this.type = Type.CALL;
        this.targetObject = targetObject;
        this.methodName = methodName;
        this.duration = duration;
        this.currentLoop = currentLoopCount;
        this.targetLoopCount = targetLoopCount;
        this.pingPong = pingPong;
        this.methodArgs = methodArgs;
        this.elapsedTime = 0;
        this.print = PrintType.NEVER;
    }

    @Override
    public String toString() {
        return "Updatable(name: " + name + " | type: " + type + " | property: " + propertyName + " | duration" + duration + " | elapsed time:" + elapsedTime + ")";
    }

    public void updateTime(double deltaTime) {
        elapsedTime += deltaTime;
    }

    public abstract void update(); // meant to be overridden
    public abstract void loop(); // resets the updatable to the beginning of the loop
    public void performOnLoopComplete() {} // meant to be overridden if an updatable wants to perform an action when the loop is complete
    public void performOnComplete() {} // meant to be overridden if an updatable wants to perform an action when the updatable is complete

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Type getType() { return type; }
    public Object getTarget() { return targetObject; }
    public void setTarget(Object targetObject) { this.targetObject = targetObject; }
    public String getPropertyName() { return propertyName; }
    public String getMethodName() { return methodName; }
    public Object[] getMethodArgs() { return methodArgs; }
    public double getDuration() { return duration; }
    public double getElapsedTime() { return elapsedTime; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isLoopComplete() { return elapsedTime >= duration; }
    public int getCurrentLoop() { return currentLoop; }
    public int getTargetLoopCount() { return targetLoopCount; }
    public boolean getPingPong() { return pingPong; }
    public PrintType getPrint() { return print; }
    public boolean isComplete() { return currentLoop >= targetLoopCount && elapsedTime >= duration && targetLoopCount >= 0 && !pingPong; }
    public void delete() {
        delete = true;
    }
    public boolean shouldDelete() {
        return delete;
    }

    public boolean modifyingSameObjectProperty(Updatable updatable) {
        return targetObject == updatable.targetObject && propertyName.equals(updatable.propertyName);
    }
}
