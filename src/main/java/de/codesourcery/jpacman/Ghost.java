package de.codesourcery.jpacman;

public class Ghost extends Entity
{
    /*
     * Ghost behaviour
     * https://gameinternals.com/understanding-pac-man-ghost-behavior
     */
    public enum Mode
    {
        DO_NOTHING,
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

    public Ghost(Personality personality)
    {
        this.mode = Mode.DO_NOTHING;
        this.personality = personality;
    }
}
