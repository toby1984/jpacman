package de.codesourcery.jpacman;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class EditorPanel extends AbstractPanel
{
    private static final int SNAP_RADIUS = 7;

    private static final boolean DRAW_GRID = false;

    private List<Line> lines = new ArrayList<>();
    private List<Line> background = new ArrayList<>();
    private List<Point> dots = new ArrayList<>();

    private Point lineStart;
    private Point lineEnd;

    private Line highlighted;

    public EditorPanel() throws IOException
    {
        background.addAll( loadLines() );

        // load dots
        File file = new File("/home/tobi/intellij_workspace/jpackman/src/main/resources/navgrid.txt");
        List<String> rows = Files.readAllLines(file.toPath());
        for ( String l : rows ) {
            final String[] parts = l.split(",");
            dots.add(new Point(Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])));
        }

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if ( highlighted != null ) {
                    if ( e.getKeyCode() == KeyEvent.VK_DELETE )
                    {
                        lines.remove( highlighted );
                        highlighted = null;
                        repaint();
                    }
                } else if ( e.getKeyCode() == KeyEvent.VK_O ) {
                    List<Line> in = new ArrayList<>(lines);
                    System.out.println( in.size()+" lines in.");
                    List<Line> out;
                    do
                    {
                        out = optimize(in);
                    } while( out.size() < in.size() );
                    System.out.println( out.size()+" lines out.");
                    lines = out;
                    repaint();
                } else if ( e.getKeyCode() == KeyEvent.VK_P ) {
                    System.out.println("-------------------------");
                    for ( Line l : lines ) {
                        System.out.println( l.start.x+","+l.start.y+","+l.end.x+","+l.end.y);
                    }
                }
                else if ( e.getKeyCode() == KeyEvent.VK_D )
                {
                    System.out.println("---------- dots ----------");
                    for ( Point p : dots ) {
                        System.out.println(p.x+","+p.y);
                    }
                }
            }
        });

        final MouseAdapter adapter = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if ( e.getButton() == MouseEvent.BUTTON1 )
                {
                    if (lineStart == null)
                    {
                        highlighted = null;
                        lineStart = viewToGrid(e.getPoint());
                    }
                    else
                    {
                        lines.add(new Line(lineStart, lineEnd));
                        lineStart = null;
                    }
                } else if ( e.getButton() == MouseEvent.BUTTON3 ) {
                    Point p = viewToGrid(e.getPoint());
                    if ( lines.stream().noneMatch(x -> x.contains(p.x,p.y ) ) )
                    {
                        if (dots.contains(p))
                        {
                            dots.remove(p);
                        }
                        else
                        {
                            dots.add(p);
                        }
                        repaint();
                    }
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                if (lineStart != null)
                {
                    highlighted = null;
                    lineEnd = viewToGrid(e.getPoint());
                    repaint();
                } else {
                    final Optional<Line> line = getLineFor(e.getPoint());
                    highlighted = line.orElse(null);
                    repaint();
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    public static List<Line> loadLines() throws IOException
    {
        List<Line> lines = new ArrayList<>();
        File file = new File("/home/tobi/intellij_workspace/jpackman/src/main/resources/lines.txt");
        List<String> rows = Files.readAllLines(file.toPath());
        for ( String l : rows ) {
            final String[] parts = l.split(",");
            lines.add( new Line( new Point(Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])),
                new Point(Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]))));
        }
        return lines;
    }

    private static List<Line> optimize(List<Line> input)
    {
        List<Line> result = new ArrayList<>();
        while ( ! input.isEmpty() ) {
            Line l = input.remove(0);
            for (Iterator<Line> iterator = input.iterator(); iterator.hasNext(); )
            {
                Line l2 = iterator.next();
                if ( l.canBeMerged(l2 ) ) {
                    l = l.merge(l2);
                    iterator.remove();
                }
            }
            result.add(l);
        }
        return result;
    }

    private Optional<Line> getLineFor(Point pWorld)
    {
        Line result = null;
        for ( Line l : lines )
        {
            final Point pMin = gridToView(l.min() );
            final Point pMax = gridToView(l.max() );
            boolean selected;
            selected = pWorld.x >= pMin.x-SNAP_RADIUS && pWorld.x <= pMax.x+SNAP_RADIUS &&
                           pWorld.y >= pMin.y-SNAP_RADIUS && pWorld.y <= pMax.y+SNAP_RADIUS;
            if ( selected ) {
                return Optional.of(l);
            }
        }
        return Optional.ofNullable(result);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        recalcCoords();

        // draw grid
        if ( DRAW_GRID )
        {
            g.setColor(Color.GRAY);
            for (int y = 0; y < HEIGHT; y++)
            {
                int py = Math.round( offsetY + y * stepY );
                for (int x = 0; x < WIDTH; x++)
                {
                    int px = Math.round( offsetX + x * stepX );
                    g.drawRect(px - 1, py - 1, 2, 2);
                }
            }
        }

        // draw lines
        g.setColor(Color.BLUE);
        background.forEach(l -> drawLine(l, g));

        g.setColor(Color.PINK);
        lines.forEach(l -> drawLine(l, g));

        // draw current line (if any)
        if ( lineStart != null && lineEnd != null ) {
            g.setColor(Color.RED);
            drawLine(lineStart,lineEnd,g);
        } else if ( highlighted != null ) {
            g.setColor(Color.YELLOW);
            drawLine(highlighted.start,highlighted.end,g);
        }

        // draw dots
        g.setColor(Color.WHITE);
        for (Point p : dots)
        {
            final Point x = gridToView(p);
            g.drawRect(x.x - 1, x.y - 1, 2, 2);
        }
    }
}
