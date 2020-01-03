package de.codesourcery.jpacman;

import java.awt.Point;

public abstract class Entity
{
    public final LocationInfo location = new LocationInfo();
    public NavGrid.Direction orientation = NavGrid.Direction.UP;
    public boolean isMoving;

    public Point gridLocation()
    {
        return gridLocation(location.position() );
    }

    public Point gridLocation(float position)
    {
        if ( position < 0.0f || position > 1.0f ) {
            throw new IllegalArgumentException();
        }
        if ( location.currentLine.isHoriz() ) {
            int minX = Math.min(location.currentLine.start.x, location.currentLine.end.x );
            int maxX = Math.max(location.currentLine.start.x, location.currentLine.end.x );
            return new Point( minX + Math.round( (maxX-minX) * position), location.currentLine.start.y );
        }
        int minY = Math.min(location.currentLine.start.y, location.currentLine.end.y );
        int maxY = Math.max(location.currentLine.start.y, location.currentLine.end.y );
        return new Point( location.currentLine.start.x,  minY + Math.round( (maxY-minY) * position ) );
    }

    public abstract void reset();
}
