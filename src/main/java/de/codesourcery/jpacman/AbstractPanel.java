package de.codesourcery.jpacman;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Point2D;

public abstract class AbstractPanel extends JPanel
{
    public static final int WIDTH  = 28;
    public static final int HEIGHT = 31;

    protected float stepX;
    protected float stepY;

    protected float offsetX;
    protected float offsetY;

    public AbstractPanel()
    {
        setForeground(Color.BLUE);
        setBackground(Color.BLACK);
        setFocusable(true);
        setPreferredSize(new Dimension(640,480));
    }

    protected final Point gridToView(Point p)
    {
        int x = (int) Math.floor( offsetX + p.x * stepX );
        int y = (int) Math.floor( offsetY + p.y * stepY );
        return new Point(x,y);
    }

    protected final Point gridToView2(Point p)
    {
        int x = (int) Math.floor( offsetX + stepX/2f + p.x * stepX );
        int y = (int) Math.floor( offsetY + stepY/2f + p.y * stepY );
        return new Point(x,y);
    }

    protected final Rectangle gridToRect(Point p)
    {
        final int cx = (int) Math.floor( offsetX + p.x * stepX - stepX/2 );
        final int cy = (int) Math.floor( offsetY + p.y * stepY - stepY/2 );

        final int w = Math.round( stepX );
        final int h = Math.round( stepY );

        return new Rectangle(cx,cy,w,h);
    }

    protected final Point gridToView(Point2D.Float p)
    {
        final int x = (int) Math.floor(offsetX + p.x * stepX);
        final int y = (int) Math.floor(offsetY + p.y * stepY);
        return new Point(x,y);
    }

    protected final Point viewToGrid(Point input)
    {
        int gridX = (int) Math.floor( (input.x - offsetX) / stepX );
        int gridY = (int) Math.floor( (input.y - offsetY) / stepY );
        return new Point( gridX, gridY );
    }

    protected final Point viewToGrid2(Point input)
    {
        int gridX = (int) Math.floor( (input.x - offsetX + stepX/2f) / stepX );
        int gridY = (int) Math.floor( (input.y - offsetY + stepY/2f) / stepY );
        return new Point( gridX, gridY );
    }

    protected final void drawLine(Line l, Graphics g) {
        drawLine(l.start,l.end,g);
    }

    protected final void drawLine(Point lineStart, Point lineEnd, Graphics g) {

        int p0X = (int) Math.floor( offsetX + lineStart.x*stepX );
        int p0Y = (int) Math.floor( offsetY + lineStart.y*stepY );
        int p1X = (int) Math.floor( offsetX + lineEnd.x*stepX );
        int p1Y = (int) Math.floor( offsetY + lineEnd.y*stepY );

        g.drawLine(p0X,p0Y,p1X,p1Y);
    }

    protected final void recalcCoords()
    {
        stepX = getWidth() / (WIDTH+2f);
        stepY = getHeight() / (HEIGHT+2f);
        offsetX = stepX;
        offsetY = stepY;
    }
}
