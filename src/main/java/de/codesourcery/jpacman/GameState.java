package de.codesourcery.jpacman;

import java.awt.Point;
import java.util.*;

public class GameState
{
    public final NavGrid navGrid = new NavGrid();
    public final Player player = new Player();

    public final DotGrid dots = new DotGrid();

    public final DotCounter globalDotCounter = new DotCounter();
    public DotCounter activeDotCounter;

    public long timeLastDotEaten;

    public final GhostStateTimer ghostStateTimer = new GhostStateTimer();

    public boolean gameOver;
    public int level;

    private final Ghost blinky = new Ghost(Ghost.Personality.BLINKY);
    private final Ghost inky = new Ghost(Ghost.Personality.INKY);
    private final Ghost clyde = new Ghost(Ghost.Personality.CLYDE);
    private final Ghost pinky = new Ghost(Ghost.Personality.PINKY);

    public final List<Ghost> ghosts = List.of(blinky,inky,clyde,pinky);

    public boolean debugMode;

    public GameState()
    {
        reset();
    }

    public void reset()
    {
        ghostStateTimer.reset(this);
        timeLastDotEaten = System.currentTimeMillis();
        level = 1;
        gameOver = false;
        dots.reset();

        player.reset();
        navGrid.assignRandomLocation(player);

        ghosts.forEach(Ghost::reset);

        globalDotCounter.reset();

        // position ghosts
        resetGhosts();

        updateDotCounterLimits();
    }

    private void resetGhosts()
    {
        ghosts.forEach(this::resetGhost);

        ghostStateTimer.reset(this);

        /*
 Whenever a life is lost, the system disables (but does not reset) the ghosts' individual dot counters and
 uses a global dot counter instead.
 This counter is enabled and reset to zero after a life is lost,
  counting the number of dots eaten from that point forward.

The three ghosts inside the house must wait for this special counter to tell them when to leave.
Pinky is released when the counter value is equal to 7 and Inky is released when it equals 17.
The only way to deactivate the counter is for Clyde to be inside the house when the counter equals 32;
otherwise, it will keep counting dots even after the ghost house is empty.
         */
        activeDotCounter = globalDotCounter;
        globalDotCounter.reset();
        globalDotCounter.setLimit(7);
        globalDotCounter.limitAction= () -> {
            getGhostFromSpawn().filter(x->x.is(Ghost.Personality.PINKY)).ifPresent(this::enableGhost);
            globalDotCounter.setLimit(17);
            globalDotCounter.limitAction= () -> {
                getGhostFromSpawn().filter(x->x.is(Ghost.Personality.INKY)).ifPresent(this::enableGhost);
                globalDotCounter.setLimit(32);
                globalDotCounter.limitAction= () ->
                {
                    getGhostFromSpawn().filter(x->x.is(Ghost.Personality.CLYDE)).ifPresent(this::enableGhost);
                    activeDotCounter = null;
                };
            };
        };
    }

    private void resetGhost(Ghost ghost)
    {
        switch(ghost.personality) {
            case BLINKY:
                setLocation(blinky, new Point(13, 11));
                break;
            case PINKY:
            case INKY:
            case CLYDE:
                // TODO: Implement positioning
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + ghost.personality);
        }
    }

    public void updateDotCounterLimits()
    {
        /*

The order of preference for choosing which ghost's counter to activate is: Pinky,
then Inky, and then Clyde.
For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its dot counter increased by one. Each ghost also has a "dot limit" associated with his counter, per level.

Pinky's dot limit is always set to zero, causing him to leave home immediately when every level begins.
 For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60.
 This results in Pinky exiting immediately which, in turn, activates Inky's dot counter.
 His counter must then reach or exceed 30 dots before he can leave the house.

 Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and starts counting dots.
 When his counter reaches or exceeds 60, he may exit.
 On the second level, Inky's dot limit is changed from 30 to zero,
 while Clyde's is changed from 60 to 50.
 Inky will exit the house as soon as the level begins from now on.

 Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game and
 will leave the ghost house immediately at the start of every level.
         */
        if ( level == 1 )
        {
            pinky.dotCounter.setLimit(0);
            inky.dotCounter.setLimit(30);
            clyde.dotCounter.setLimit(60);
        }
        else if ( level == 2 )
        {
            inky.dotCounter.setLimit(0);
            clyde.dotCounter.setLimit(50);
        } else if ( level >= 3 ) {
            pinky.dotCounter.setLimit(0);
            inky.dotCounter.setLimit(0);
            clyde.dotCounter.setLimit(0);
        }
    }

    private void setLocation(Ghost ghost,Point p)
    {
        List<Line> lines = navGrid.getLines(p);
        ghost.location.currentLine = lines.get(0);
        ghost.location.setPosition( ghost.location.currentLine.getPosition(p) );
    }

    public void tick(Set<PlayingField.Input> input,float elapsedSeconds)
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
                return;
            }

            if ( input.contains(PlayingField.Input.RESTART)) {
                input.clear();
                reset();
                return;
            }

            // handle player movement
            boolean dotEaten = false;
            if (input.contains(PlayingField.Input.UP) && maybeMove(NavGrid.Direction.UP))
            {
                dotEaten = playerMoved();
            }
            else if (input.contains(PlayingField.Input.DOWN) && maybeMove(NavGrid.Direction.DOWN))
            {
                dotEaten = playerMoved();
            }
            else if (input.contains(PlayingField.Input.LEFT) && maybeMove(NavGrid.Direction.LEFT))
            {
                dotEaten = playerMoved();
            }
            else if (input.contains(PlayingField.Input.RIGHT) && maybeMove(NavGrid.Direction.RIGHT))
            {
                dotEaten = playerMoved();
            }

            if ( dotEaten )
            {
                if ( dots.allEaten() ) {
                    // TODO: What to do if last eaten dot is a power-up ? Let the player try to catch the ghosts or just reset & advance to next level ?
                    advanceToNextLevel();
                }
            }
            else
            {
                long elapsedMillis = System.currentTimeMillis() - timeLastDotEaten;
                /*
The game begins with an initial timer limit of four seconds,
but lowers to it to three seconds starting with level five.
                 */
                final long threshold = level < 5 ? 4000 : 3000;
                if ( elapsedMillis > threshold )
                {
                    timeLastDotEaten = System.currentTimeMillis();
                    forceReleaseGhost();
                }
            }
        }

        // handle ghost movement
        final Point playerLocation = player.gridLocation();
        for ( Ghost ghost : ghosts )
        {
            ghost.tick(this);
            if ( ghost.isAlive() && playerLocation.equals(ghost.gridLocation()))
            {
                if (player.canEatGhosts)
                {
                    // --> ghost dies
                    ghostEaten(ghost);
                }
                else
                {
                    // --> player dies
                    playerDeath();
                }
            }
        }

        // revert ghost behavior if player can no
        // longer eat ghosts
        if ( player.canEatGhosts )
        {
            long elapsed = System.currentTimeMillis() - player.startTimeCanEatGhosts;
            if ( elapsed >= 4000 )
            {
                player.canEatGhosts = false;
                ghosts.stream().filter(Ghost::isAlive).forEach(gh -> gh.setMode(Ghost.Mode.SCATTER));
            }
        }
    }

    private void incScore(int points) {
        player.score += points;
        if ( player.score >= 10000 && ! player.bonusLifeAwarded ) {
            player.lifes++;
            player.bonusLifeAwarded = true;
        }
    }

    private void ghostEaten(Ghost ghost)
    {
        resetGhost(ghost);
        if ( activeDotCounter == null ) {
            activeDotCounter = ghost.dotCounter;
            ghost.dotCounter.reset();
            ghost.dotCounter.limitAction = () -> {

            };
        }
    }

    private void playerDeath() {

        player.lifes--;
        if ( player.lifes == 0 ) {
                gameOver = true;
        }
        else
        {
            resetGhosts();
        }
    }

    private void enableGhost(Ghost ghost)
    {
        ghost.setMode(Ghost.Mode.SCATTER);
        ghost.selectTargetTile(this);
    }

    private void forceReleaseGhost()
    {
        getGhostFromSpawn().ifPresent(toRelease ->
        {
            toRelease.setMode(Ghost.Mode.CHASING);
            toRelease.selectTargetTile(this);
            getGhostFromSpawn().ifPresent(gh -> activeDotCounter = gh.dotCounter );
        });
    }

    private Optional<Ghost> getGhostFromSpawn() {
        // The order of preference for choosing which ghost's counter to activate is: Pinky, then Inky, and then Clyde.
        if ( pinky.hasMode(Ghost.Mode.WAITING_AT_SPAWN ) ) {
            return Optional.of(pinky);
        }
        else if ( inky.hasMode(Ghost.Mode.WAITING_AT_SPAWN ) ) {
            return Optional.of(inky);
        }
        else if ( clyde.hasMode(Ghost.Mode.WAITING_AT_SPAWN ) ) {
            return Optional.of(clyde);
        }
        return Optional.empty();
    }

    // return: true if a dot was consumed
    private boolean playerMoved()
    {
        player.isMoving = true;
        final DotGrid.Dot eatenDot = dots.consume(player.gridLocation() );
        if ( eatenDot == null ) {
            return false;
        }
        timeLastDotEaten = System.currentTimeMillis();

        if ( eatenDot.isEnergizer )
        {
            incScore(50);

            // switch player to "ghosts can be eaten" mode
            if ( level < 19 )
            {
                player.canEatGhosts = true;
                player.startTimeCanEatGhosts = System.currentTimeMillis();

                // Switch all ghosts to "Frightened" mode
                ghosts.stream().filter(Ghost::isAlive).forEach(gh -> gh.setMode(Ghost.Mode.FRIGHTENED));
            }
        } else {
            incScore(10);
        }
        if ( activeDotCounter != null )
        {
            activeDotCounter.dotEaten();
        }
        return true;
    }

    private void advanceToNextLevel()
    {
        player.canEatGhosts = false;
        level++;
        dots.reset();
        resetGhosts();
        navGrid.assignRandomLocation(player);
        globalDotCounter.reset();
        activeDotCounter = globalDotCounter;

        ghostStateTimer.difficultyChanged(this);
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