package de.codesourcery.jpacman;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DotGrid
{
    public List<Dot> dots = new ArrayList<>();

    public static final class Dot extends Point
    {
        public boolean isEaten;

        public Dot(int x,int y) {
            super(x,y);
        }
    }

    public DotGrid()
    {
        reset();
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
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                dots.add(new Dot(x, y));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean consume(Point p)
    {
        for ( Dot d : dots )
        {
            if ( d.x == p.x && d.y == p.y )
            {
                if ( d.isEaten ) {
                    return false;
                }
                d.isEaten = true;
                return true;
            }
        }
        return false;
    }
}
