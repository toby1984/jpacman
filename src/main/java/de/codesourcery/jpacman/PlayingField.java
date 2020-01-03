package de.codesourcery.jpacman;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class PlayingField extends AbstractPanel
{
    private static final int TICKS_PER_ANIMATION_STATE = 2;
    private static final int ANIMATION_STATE_COUNT = 7;

    private static final boolean DRAW_HIGHLIGHT = true;

    private static final AffineTransform IDENTITY = new AffineTransform();

    private final GameState state;

    private static final boolean DRAW_NAV_GRID = false;
    private final List<Line> lines;

    private int tickCount;
    private int animationState=0;

    public enum Input {
        UP,DOWN,LEFT,RIGHT,RESTART,DEBUG;
    }

    public final Set<Input> userInput = new HashSet<>();

    private Point highlighted = null;

    public PlayingField(GameState state) throws IOException
    {
        this.state = state;
        lines = EditorPanel.loadLines();
        setBackground(Color.BLACK);

        addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                if ( DRAW_HIGHLIGHT )
                {
                    Point pNew = viewToGrid2(e.getPoint());
                    if (!Objects.equals(highlighted, pNew))
                    {
                        highlighted = pNew;
                        repaint();
                    }
                }
            }
        });
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                mapToInput(e).ifPresent(userInput::add);
            }

            private Optional<Input> mapToInput(KeyEvent e)
            {
                switch( e.getKeyCode() ) {
                    case KeyEvent.VK_A:
                        return Optional.of(Input.LEFT);
                    case KeyEvent.VK_D:
                        return Optional.of(Input.RIGHT);
                    case KeyEvent.VK_W:
                        return Optional.of(Input.UP);
                    case KeyEvent.VK_S:
                        return Optional.of(Input.DOWN);
                    case KeyEvent.VK_ENTER:
                        return Optional.of(Input.RESTART);
                    case KeyEvent.VK_SPACE:
                        return Optional.of(Input.DEBUG);
                }
                return Optional.empty();
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                mapToInput(e).ifPresent(userInput::remove);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintComponent((Graphics2D ) g );
    }

    private void paintComponent(Graphics2D g)
    {
        super.paintComponent(g);
        recalcCoords();

        // draw lines
        g.setColor(Color.BLUE);
        lines.forEach(l -> drawLine(l, g));

        // draw dots
        for (DotGrid.Dot p : state.dots.dots)
        {
            if ( ! p.isEaten )
            {
                final Point x = gridToView(p);
                if ( p.isEnergizer )
                {
                    g.setColor(Color.PINK);
                    int w = (int) Math.min(stepX,stepY);
                    g.fillArc(x.x - w/2, x.y - w/2, w, w,0,360);
                }
                else
                {
                    g.setColor(Color.WHITE);
                    g.drawRect(x.x - 1, x.y - 1, 2, 2);
                }
            }
        }

        // draw player
        final Point p0 = gridToView(state.player.location.currentLine.start);
        final Point p1 = gridToView(state.player.location.currentLine.end);

        final Point playerLoc;
        if ( state.player.location.currentLine.isHoriz() ) {
            int minX = Math.min(p0.x,p1.x);
            int maxX = Math.max(p0.x,p1.x);
            int px = minX + Math.round( (maxX-minX)*state.player.location.position() );
            playerLoc = new Point(px,p0.y);
        } else {
            int minY = Math.min(p0.y,p1.y);
            int maxY = Math.max(p0.y,p1.y);
            int py = minY + Math.round( (maxY-minY) * state.player.location.position() );
            playerLoc = new Point(p0.x,py);
        }

        final int playerSize = (int) Math.min(stepX, stepY);
        drawPlayer(playerLoc.x, playerLoc.y, playerSize,state.player.orientation,g );

        g.setColor(Color.WHITE);
        g.setFont( new Font(Font.MONOSPACED,Font.BOLD,12));
        g.drawString("Score: "+state.player.score, 5, stepY);
//        g.drawString("Player "+state.player.orientation+" @ "+state.player.location,10,10);

        if ( DRAW_HIGHLIGHT && highlighted != null ) {
            final Rectangle r = gridToRect( highlighted );
            g.setColor(Color.GREEN);
            g.fillRect(r.x,r.y,r.width,r.height);
            g.drawString("Highlighted: "+highlighted, 10, 20);
        }

        if ( state.debugMode )
        {
            g.setColor(Color.GRAY);
            for ( int y = 0 ; y < state.navGrid.height ; y++ )
            {
                for (int x = 0; x < state.navGrid.width; x++)
                {
                    final Rectangle r = gridToRect(new Point(x, y));
                    g.drawRect(r.x, r.y, r.width, r.height);
                }
            }
        }

        // draw nav grid
        if (DRAW_NAV_GRID)
        {
            g.setColor(Color.PINK);
            for (de.codesourcery.jpacman.Line l : state.navGrid.lines)
            {
                Point start = gridToView(l.start);
                Point end = gridToView(l.end);
                g.drawLine(start.x, start.y, end.x, end.y);
            }
        }

        if (state.player.isMoving)
        {
            if ( (++tickCount % TICKS_PER_ANIMATION_STATE) == 0)
            {
                animationState = (animationState + 1) % ANIMATION_STATE_COUNT;
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawPlayer(int x, int y, int radius, NavGrid.Direction orientation, Graphics2D g)
    {
        g.setColor(Color.YELLOW);

        float maxMouthAngle = 90f;
        float anglePerState = maxMouthAngle / ANIMATION_STATE_COUNT;
        float angle = anglePerState * animationState;

        AffineTransform t;
        switch ( orientation ) {
            case UP:
                t = AffineTransform.getTranslateInstance(x,y);
                t.rotate(Math.toRadians(-90+22.5));
                t.translate(-x,-y );
                break;
            case DOWN:
                t = AffineTransform.getTranslateInstance(x,y);
                t.rotate(Math.toRadians(90+22.5));
                t.translate(-x,-y );
                break;
            case LEFT:
                t = AffineTransform.getTranslateInstance(x,y);
                t.rotate(Math.toRadians(180+22.5));
                t.translate(-x,-y );
                break;
            case RIGHT:
                t = AffineTransform.getTranslateInstance(x,y);
                t.rotate(Math.toRadians(22.5));
                t.translate(-x,-y );
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + orientation);
        }

        g.setTransform(t);
        radius *= 2;
        g.fillArc(x - radius / 2, y - radius / 2, radius, radius, (int) (angle), (int) (360 - angle));
        g.setTransform(IDENTITY);
    }
}