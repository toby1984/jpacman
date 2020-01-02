package de.codesourcery.jpacman;

import java.awt.Point;
import java.util.Objects;

public final class Line
{
    public Point start,end;

    public Line(Point start, Point end)
    {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( o instanceof  Line)
        {
            final Line line = (Line) o;
            return (start.equals(line.start) && end.equals(line.end)) || (start.equals(line.end) && end.equals(line.start));
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int c1 = Objects.hash(start.x, start.y );
        int c2 = Objects.hash(end.x, end.y );
        return c1 ^ c2;
    }

    @Override
    public String toString()
    {
        return toString(start) + " -> " + toString(end);
    }

    public float getPosition(Point p) {

        float delta;
        float len;
        float percent;
        if ( isHoriz() ) {
            if ( p.y != start.y ) {
                throw new IllegalArgumentException();
            }
            int minX = Math.min(start.x, end.x);
            int maxX = Math.max(start.x, end.x);
            len = maxX - minX;
            delta = p.x - minX;
        }
        else
        {
            if (p.x != start.x)
            {
                throw new IllegalArgumentException();
            }
            int minY = Math.min(start.y, end.y);
            int maxY = Math.max(start.y, end.y);
            len = maxY - minY;
            delta = p.y - minY;
        }
        percent = delta / len;
        if ( percent < 0 || percent > 1.0f ) {
            throw new IllegalArgumentException();
        }
        return percent;
    }

    private static String toString(Point p) {
        return "("+p.x+","+p.y+")";
    }

    public boolean isEndpoint(Point p) {
        return isEndpoint(p.x,p.y);
    }

    public boolean isEndpoint(int x,int y) {
        return start.x == x && start.y == y || end.x == x && end.y == y;
    }

    public boolean contains(Point p) {
        return contains(p.x,p.y);
    }

    public boolean contains(int localX,int localY)
    {
        final Point min = min();
        final Point max = max();

        if ( isHoriz() ) {
            return localY == start.y && localX >= min.x && localX <= max.x;
        }
        return localX == start.x && localY >= min.y && localY <= max.y;
    }

    public boolean canBeMerged(Line other) {
        if ( this.isHoriz() != other.isHoriz() ) {
            return false;
        }
        return this.start.equals( other.start ) ||
                   this.start.equals( other.end ) ||
                   this.end.equals( other.start ) ||
                   this.end.equals( other.end );
    }

    public float length()
    {
        int dx = end.x - start.x;
        int dy = end.y - start.y;
        return (float) Math.sqrt( dx*dx + dy*dy );
    }

    public Line merge(Line other) {
        if ( ! canBeMerged(other ) ) {
            throw new UnsupportedOperationException();
        }
        if ( isHoriz() )
        {
            int minX = Math.min( Math.min(this.start.x, other.start.x), Math.min(this.end.x, other.end.x ) );
            int maxX = Math.max( Math.max(this.start.x, other.start.x), Math.max(this.end.x, other.end.x ) );

            return new Line(new Point(minX, this.start.y), new Point(maxX, this.start.y));
        }
        int minY = Math.min( Math.min(this.start.y, other.start.y), Math.min(this.end.y, other.end.y ) );
        int maxY = Math.max( Math.max(this.start.y, other.start.y), Math.max(this.end.y, other.end.y ) );

        return new Line(new Point(this.start.x,minY), new Point(this.start.x, maxY));
    }

    public boolean isHoriz() {
        return start.y == end.y;
    }

    public boolean isVertical() {
        return start.x == end.x;
    }

    public Point min() {
        return new Point(Math.min(start.x,end.x),Math.min(start.y,end.y));
    }

    public Point max() {
        return new Point(Math.max(start.x,end.x),Math.max(start.y,end.y));
    }
}
