package de.codesourcery.jpacman;

import java.awt.Point;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NavGrid
{
    public final List<Line> lines = new ArrayList<>();

    public int width;
    public int height;

    public Line toLeftExit;
    public Line toRightExit;

    public enum Direction
    {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);
        public final int dx,dy;

        Direction(int dx, int dy)
        {
            this.dx = dx;
            this.dy = dy;
        }

        public boolean isHoriz() {
            return this == LEFT || this == RIGHT;
        }

        public boolean isVertical() {
            return this == UP || this == DOWN;
        }
    }

    public boolean isLineToLeftExit(Line l) {
        return l.isEndpoint(0,14 ) && l.isEndpoint(9,14);
    }

    public boolean isLineToRightExit(Line l) {
        return l.isEndpoint(18,14 ) && l.isEndpoint(27,14);
    }

    public NavGrid()
    {
        try ( final InputStream file = NavGrid.class.getResourceAsStream("/navgrid.txt") ) {
            if ( file == null ) {
                throw new RuntimeException("Failed to load navgrid");
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(file) );
            String line;
            while ( ( line = reader.readLine() ) != null) {
                final String[] parts = line.split(",");
                final Point p0 = new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
                final Point p1 = new Point(Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
                lines.add( new Line(p0,p1 ) );
            }
            if ( ! lines.isEmpty() )
            {
                toLeftExit = lines.stream().filter(this::isLineToLeftExit).findFirst().orElseThrow(() -> new RuntimeException("No left exit"));
                toRightExit = lines.stream().filter(this::isLineToRightExit).findFirst().orElseThrow(() -> new RuntimeException("No right exit"));

                final Line l1 = lines.get(0);
                Point p0 = l1.min();
                Point p1 = l1.max();
                int minX = p0.x;
                int maxX = p1.x;
                int minY = p0.y;
                int maxY = p1.y;

                for (int i = 1, dotsSize = lines.size(); i < dotsSize; i++)
                {
                    Line l = lines.get(i);
                    p0 = l.min();
                    p1 = l.max();
                    minX = Math.min(minX,p0.x);
                    minY = Math.min(minY,p0.y);
                    maxX = Math.max(maxX,p1.x);
                    maxY = Math.max(maxY,p1.y);
                }
                width = maxX - minX;
                height = maxY - minY;
                System.out.println("minX / maxX: " + minX + "/" + maxX);
                System.out.println("minY / maxY: " + minY + "/" + maxY);
                System.out.println("WIDTH: "+width);
                System.out.println("HEIGHT: "+height);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean canSwitchLines(Point currentPos)
    {
        return getLines(currentPos).size() > 1;
    }

    public Set<Direction> getAvailableDirections(Point currentPos)
    {
        List<Line> matches = getLines(currentPos);
        List<Line> endpoints = matches.stream().filter( x -> x.isEndpoint(currentPos ) ).collect(Collectors.toList());

        final Set<Direction> result = new HashSet<>(4);
        if ( endpoints.size() > 1 ) {

        } else if ( endpoints.size() == 1 ) {

        } else {
            // not on an endpoint
            if ( matches.size() != 1 ) {
                throw new IllegalStateException();
            }

            final Line l = matches.get(0);
            Point p0 = l.min();
            Point p1 = l.max();
            if ( l.isHoriz() )
            {
                if ( p0.x == currentPos.x )
                {
                    result.add( Direction.RIGHT );
                }
                else if ( p1.x == currentPos.x )
                {
                    result.add(Direction.LEFT);
                }
                else
                {
                    result.add(Direction.LEFT);
                    result.add(Direction.RIGHT);
                }
            } else {
                if ( p0.y == currentPos.y )
                {
                    result.add( Direction.DOWN );
                }
                else if ( p1.y == currentPos.y )
                {
                    result.add(Direction.UP);
                }
                else
                {
                    result.add(Direction.UP);
                    result.add(Direction.DOWN);
                }
            }

        }
        return result;
    }

    public List<Line> getLines(Point currentPos)
    {
        return lines.stream().filter(l -> l.contains(currentPos)).collect(Collectors.toList());
    }

    public void assignRandomLocation(Player p)
    {
        final Random rndGen = new Random();
        final int rnd = rndGen.nextInt(lines.size());
        p.location.currentLine = lines.get(rnd);
        p.location.setPosition( rndGen.nextFloat() );

        System.out.println("Player is at " + p.location+" -> "+p.gridLocation());
        if ( p.location.currentLine.isHoriz() )
        {
            p.orientation = Direction.LEFT;
        } else {
            p.orientation = Direction.UP;
        }
    }
}
