package engine.core;
import engine.helper.MarioActions;
import java.util.*;

public class MarioAgentEvent {
    private int[][] observations;
    private boolean[] actions;
    private float marioX;
    private float marioY;
    private int marioState;
    private boolean marioOnGround;
    private int time;

    public MarioAgentEvent(boolean[] actions, float marioX, float marioY, int marioState, boolean marioOnGround, int time,  int[][] observations) {
        this.actions = actions;
        this.marioX = marioX;
        this.marioY = marioY;
        this.marioState = marioState;
        this.marioOnGround = marioOnGround;
        this.time = time;
        this.observations = observations;
    }

    public int[][] getObservations() {
        return this.observations;
    }

    public float getX() {
        return this.marioX;
    }

    public float getY() {
        return this.marioY;
    }

    public String getActionString() {
        String s = "";
        if (this.actions[MarioActions.RIGHT.getValue()])
            s += "Forward ";
        if (this.actions[MarioActions.LEFT.getValue()])
            s += "Backward ";
        if (this.actions[MarioActions.SPEED.getValue()])
            s += "Speed ";
        if (this.actions[MarioActions.JUMP.getValue()])
            s += "Jump ";
        if (this.actions[MarioActions.DOWN.getValue()])
            s += "Duck";
        if (s.length() == 0) {
            s = "[NONE]";
        }
        return s.trim();
    }

    public boolean[] getActions() {
        return this.actions;
    }

    public float getMarioX() {
        return this.marioX;
    }

    public float getMarioY() {
        return this.marioY;
    }

    public int getMarioState() {
        return this.marioState;
    }

    public boolean getMarioOnGround() {
        return this.marioOnGround;
    }

    public int getTime() {
        return this.time;
    }
}
