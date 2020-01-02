package de.codesourcery.jpacman;

import java.awt.Point;
import java.util.*;

public class GameState
{
    public final NavGrid navGrid = new NavGrid();
    public final Player player = new Player();
    public final DotGrid dots = new DotGrid();
    public boolean gameOver;

    public boolean debugMode;

    public GameState() {
        reset();
    }
    public void reset()
    {
        gameOver = false;
        dots.reset();
        player.reset();
        navGrid.assignRandomLocation(player);
    }

    public void tick(Set<PlayingField.Input> input)
    {
        player.isMoving = false;
        if ( gameOver ) {
            return;
        }

        if ( ! input.isEmpty() )
        {
            if ( input.contains( PlayingField.Input.DEBUG ) ) {
                debugMode = ! debugMode;
                input.clear();
            }
            else if ( input.contains(PlayingField.Input.RESTART)) {
                input.clear();
                reset();
            }
            else
            {
                if (input.contains(PlayingField.Input.UP) && maybeMove(NavGrid.Direction.UP))
                {
                    player.isMoving = true;
                    if ( dots.consume(player.gridLocation() ) ) {
                        player.score += 100;
                    }
                }
                else if (input.contains(PlayingField.Input.DOWN) && maybeMove(NavGrid.Direction.DOWN))
                {
                    player.isMoving = true;
                    if ( dots.consume(player.gridLocation() ) ) {
                        player.score += 100;
                    }
                }
                else if (input.contains(PlayingField.Input.LEFT) && maybeMove(NavGrid.Direction.LEFT))
                {
                    player.isMoving = true;
                    if ( dots.consume(player.gridLocation() ) ) {
                        player.score += 100;
                    }
                }
                else if (input.contains(PlayingField.Input.RIGHT) && maybeMove(NavGrid.Direction.RIGHT))
                {
                    player.isMoving = true;
                    if ( dots.consume(player.gridLocation() ) ) {
                        player.score += 100;
                    }
                }
            }
        }
    }

    private static float stepSize(Line line)
    {
        return 0.1f/line.length();
    }

    private static float getFactor(NavGrid.Direction newDir) {
        return newDir == NavGrid.Direction.LEFT || newDir == NavGrid.Direction.UP ? -1 : 1;
    }

    private static Set<NavGrid.Direction> getPossibleDirections(Line line,Point pointOnLine, Set<NavGrid.Direction> result)
    {
        if ( ! line.contains(pointOnLine) ) {
            throw new IllegalArgumentException();
        }

        Point min = line.min();
        Point max = line.max();
        if ( line.isHoriz() ) {

            if ( pointOnLine.x > min.x && pointOnLine.x < max.x ) {
                result.add(NavGrid.Direction.LEFT);
                result.add(NavGrid.Direction.RIGHT);
                return result;
            }
            if ( pointOnLine.x == min.x ) {
                result.add(NavGrid.Direction.RIGHT);
                return result;
            }
            result.add(NavGrid.Direction.LEFT);
            return result;
        }
        // vertical line
        if ( pointOnLine.y > min.y && pointOnLine.y < max.y ) {
            result.add(NavGrid.Direction.UP);
            result.add(NavGrid.Direction.DOWN);
            return result;
        }
        if ( pointOnLine.y == min.y ) {
            result.add(NavGrid.Direction.DOWN);
            return result;
        }
        result.add(NavGrid.Direction.UP);
        return result;
    }

    private boolean maybeMove(NavGrid.Direction desiredDirection) {
        return maybeMove(desiredDirection, player, navGrid );
    }

    private static boolean maybeMove(NavGrid.Direction desiredDirection, Entity entity, NavGrid navGrid)
    {
        final Set<NavGrid.Direction> availableDirections = new HashSet<>(4);
        final Point locationToCheck = entity.gridLocation();

        final List<Line> intersectingLines = navGrid.getLines(locationToCheck);
        intersectingLines.forEach(l -> getPossibleDirections(l, locationToCheck,availableDirections));

        // System.out.println("Directions @ "+locationToCheck+": "+availableDirections+" { "+intersectingLines+" }");

        if ( ! availableDirections.contains(desiredDirection) )
        {
            if ( entity.location.isAtEndpoint() )
            {
                if ( navGrid.isLineToLeftExit( entity.location.currentLine ) && entity.location.position() == 0.0f && desiredDirection == NavGrid.Direction.LEFT)
                {
                    entity.location.currentLine = navGrid.toRightExit;
                    entity.location.setPosition(1.0f);
                    return true;
                }
                else if ( navGrid.isLineToRightExit( entity.location.currentLine ) && entity.location.position() == 1.0f && desiredDirection == NavGrid.Direction.RIGHT)
                {
                    entity.location.currentLine = navGrid.toLeftExit;
                    entity.location.setPosition(0f);
                    return true;
                }
                return false;
            }
            if ( (entity.location.currentLine.isHoriz()    && desiredDirection.isHoriz() ) ||
                 (entity.location.currentLine.isVertical() && desiredDirection.isVertical()))
            {
                entity.location.incPosition(getFactor(desiredDirection) * stepSize(entity.location.currentLine));
                return true;
            }
            return false;
        }

        final Set<NavGrid.Direction> dirsOnThisLine =
            getPossibleDirections(entity.location.currentLine, locationToCheck, new HashSet<>());

        if ( dirsOnThisLine.contains( desiredDirection ) )
        {
            // player stays on current line
            entity.location.incPosition( getFactor(desiredDirection) * stepSize(entity.location.currentLine) );
        }
        else
        {
            // switch to new line
            // 1. find all lines whose endpoint is the player's current location
            final List<Line> choices = new ArrayList<>(intersectingLines);
            choices.remove( entity.location.currentLine );

            Optional<Line> newLine;
            Point newLoc = new Point(locationToCheck);
            float newPos;

            if ( choices.size() == 1 ) {
                newLine = Optional.of( choices.get(0) );
                newPos = newLine.get().getPosition(newLoc);
            }
            else
            {
                switch (desiredDirection)
                {
                    case UP:
                        // same X coordinate, y = player.pos.y - 1;
                        newLoc.y -= 1;
                        newPos = 1.0f;
                        break;
                    case DOWN:
                        // same X coordinate, y = player.pos.y + 1;
                        newLoc.y += 1;
                        newPos = 0f;
                        break;
                    case LEFT:
                        // same Y coordinate, x = player.pos.x - 1;
                        newLoc.x -= 1;
                        newPos = 1.0f;
                        break;
                    case RIGHT:
                        // same Y coordinate, x = player.pos.x + 1;
                        newLoc.x += 1;
                        newPos = 0f;
                        break;
                    default:
                        throw new IllegalStateException();
                }
                newLine = choices.stream().filter(x -> x.contains(newLoc)).findFirst();
            }
            if ( newLine.isEmpty() ) {
                throw new IllegalStateException();
            }
            entity.location.currentLine = newLine.get();
            entity.location.setPosition( newPos );
        }
        entity.orientation = desiredDirection;
        return true;
    }
}