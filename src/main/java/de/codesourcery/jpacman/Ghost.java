package de.codesourcery.jpacman;

import java.awt.Point;

public class Ghost extends Entity
{
    private final Point targetTile = new Point();

    /*
     * Ghost behaviour
     * https://gameinternals.com/understanding-pac-man-ghost-behavior
     */
    public enum Mode
    {
        WAITING_AT_SPAWN,
        RETURNING_TO_SPAWN,
        CHASING,
        FRIGHTENED,
        SCATTER
    }

    public enum Personality
    {
        PINKY, // pink
        INKY, // blue
        BLINKY, // red
        CLYDE // orange
    }

    public Mode mode;
    public Personality personality;

    public final DotCounter dotCounter = new DotCounter();

    public Ghost(Personality personality)
    {
        this.mode = Mode.WAITING_AT_SPAWN;
        this.personality = personality;
    }

    public boolean isAlive() {
        return ! isDead();
    }

    public boolean isDead() {
        return hasMode(Mode.WAITING_AT_SPAWN) || hasMode(Mode.RETURNING_TO_SPAWN);
    }

    public boolean is(Personality p) {
        return p.equals( this.personality );
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    public boolean hasMode(Mode m) {
        return m.equals( this.mode );
    }

    public void selectTargetTile(GameState state)
    {
        // TODO: Implement target tile selection based on personality
        // see https://gameinternals.com/understanding-pac-man-ghost-behavior
    }

    public void tick(GameState state) {
        // TODO: Implement ghost movement
        // see https://gameinternals.com/understanding-pac-man-ghost-behavior
    }

    @Override
    public void reset()
    {
        dotCounter.reset();
    }
}