package de.codesourcery.jpacman;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DotGrid
{
    public final List<Dot> dots = new ArrayList<>();

    public static final class Dot extends Point
    {
        public final boolean isEnergizer;
        public boolean isEaten;

        public Dot(int x,int y,boolean isEnergizer) {
            super(x,y);
            this.isEnergizer = isEnergizer;
        }
    }

    public DotGrid()
    {
        reset();
    }

    public boolean allEaten()
    {
        return dots.stream().allMatch(x -> x.isEaten);
    }

    public void reset()
    {
        dots.clear();
        try ( final InputStream file = NavGrid.class.getResourceAsStream("/dots.txt") ) {
            if ( file == null ) {
                throw new RuntimeException("Failed to load dots");
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(file) );
            String line;
            while ( ( line = reader.readLine() ) != null) {
                final String[] parts = line.split(",");
                boolean isEnergizer = false;
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                if ( parts.length > 2 ) {
                    System.out.println("Energizer @ "+x+","+y);
                    isEnergizer = true;
                }
                dots.add(new Dot(x, y, isEnergizer));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dot consume(Point p)
    {
        for ( Dot d : dots )
        {
            if ( d.x == p.x && d.y == p.y )
            {
                if ( d.isEaten ) {
                    return null;
                }
                d.isEaten = true;
                return d;
            }
        }
        return null;
    }
}
