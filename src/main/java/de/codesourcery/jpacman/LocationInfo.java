package de.codesourcery.jpacman;

public class LocationInfo
{
    public Line currentLine;

    // relative position on this line in percent (0...1)
    // - for horizontal lines, 0% means player is at the left-most point
    // - for vertical lines, 0% means player is at the top-most point
    private float position;

    public float position() {
        return position;
    }

    public boolean isAtEndpoint() {
        return position == 0.0f || position == 1.0f;
    }

    public void incPosition(float delta) {
        setPosition( position + delta );
    }

    public void setPosition(float p) {
        this.position = Math.max(0f,Math.min(1.0f,p));
    }

    @Override
    public String toString()
    {
        return "LocationInfo{" +
                   "currentLine=" + currentLine +
                   ", position=" + position +
                   '}';
    }
}
