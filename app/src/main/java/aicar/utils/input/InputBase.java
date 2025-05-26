package aicar.utils.input;

public class InputBase {

    public static enum State {
        DOWN,
        UP,
        CLICKED,
        RELEASED
    }

    private int downTime = 0;
    private int upTime = 0;

    private State state;
    
    public void update() {
        if (down()) {
            downTime++;
        }
        else {
            upTime++;
        }
        if (downTime == 1) state = State.CLICKED;
        else if (upTime == 1) state = State.RELEASED;
        else state = downTime > 0 ? State.DOWN : State.UP;
    }


    public State getState() {
        return state;
    }
    public void setDown(boolean down) {
        this.state = down ? State.DOWN : State.UP;
        if (down) {
            upTime = 0;
        }
        else {
            downTime = 0;
        }
    }

    public boolean down() {
        return state == State.DOWN || state == State.CLICKED;
    }
    public boolean clicked() {
        return state == State.CLICKED;
    }
    public boolean released() {
        return state == State.RELEASED;
    }
}
