package de.codesourcery.jpacman;

public class GhostStateTimer
{
    private static class StateTransition
    {
        private final Ghost.Mode ghostMode;
        private final float durationSeconds;
        protected StateTransition next;

        private float remainingSeconds;

        public StateTransition(Ghost.Mode ghostMode,float durationSeconds) {
            this.ghostMode = ghostMode;
            this.durationSeconds = durationSeconds;
            this.remainingSeconds = durationSeconds;
        }

        public StateTransition andThen(StateTransition other) {
            this.next = other;
            return other;
        }

        public void onStateEnter(GameState state) {
            state.ghosts.stream().filter(Ghost::isAlive).forEach(gh -> gh.setMode(ghostMode));
        }

        public StateTransition tick(GameState state,float elapsedSeconds) {
            this.remainingSeconds -= elapsedSeconds;
            if (remainingSeconds <= 0)
            {
                if ( next != null )
                {
                    return next;
                }
                return GhostStateTimer.getStatesForLevel(state.level);
            }
            return this;
        }
    }

    private static class PermanentChase extends StateTransition
    {
        public PermanentChase()
        {
            super(Ghost.Mode.CHASING, Integer.MAX_VALUE);
            this.next = this;
        }
    }


    private StateTransition currentState;

    public void reset(GameState state)
    {
        difficultyChanged(state);
    }

    public void tick(GameState state, float elapsedSeconds)
    {
        updateState( currentState.tick(state, elapsedSeconds ) , state );
    }

    private void updateState(StateTransition newState, GameState state)
    {
        if ( newState != currentState ) {
            newState.onStateEnter(state);
            currentState = newState;
        }
    }

    public void difficultyChanged(GameState state)
    {
        currentState = getStatesForLevel(state.level);
        currentState.onStateEnter(state);
    }

    private static StateTransition getStatesForLevel(int level) {
            /*
Changes between Chase and Scatter modes occur on a fixed timer, which causes the "wave" effect described by Iwatani. This timer is reset at the beginning of each level and whenever a life is lost. The timer is also paused while the ghosts are in Frightened mode, which occurs whenever Pac-Man eats an energizer. When Frightened mode ends, the ghosts return to their previous mode, and the timer resumes where it left off. The ghosts start out in Scatter mode, and there are four waves of Scatter/Chase alternation defined, after which the ghosts will remain in Chase mode indefinitely (until the timer is reset). For the first level, the durations of these phases are:

    Scatter for 7 seconds, then Chase for 20 seconds.
    Scatter for 7 seconds, then Chase for 20 seconds.
    Scatter for 5 seconds, then Chase for 20 seconds.
    Scatter for 5 seconds, then switch to Chase mode permanently.

The durations of these phases are changed somewhat when the player reaches level 2,
and once again when they reach level 5.

Starting on level 2, the third Chase mode lengthens considerably, to 1033 seconds (17 minutes and 13 seconds),
and the following Scatter mode lasts just 1/60 of a second before the ghosts proceed to their permanent Chase mode.

 The level 5 changes build on top of this, additionally reducing the first two
 Scatter lengths to 5 seconds, and adding the 4 seconds gained here to the third Chase mode, lengthening it to 1037 seconds (17:17).
 Regarding the 1/60-of-a-second Scatter mode on every level except the first,
 even though it may seem that switching modes for such an insignificant amount of time is pointless,
 there is a reason behind it, which shall be revealed shortly.
     */
        StateTransition result;
        if ( level == 1 )
        {
            result = new StateTransition(Ghost.Mode.SCATTER, 7);
            result
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 7))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 5))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 5))
                .andThen(new PermanentChase());
        }
        else if ( level < 5 ) {
            result = new StateTransition(Ghost.Mode.SCATTER, 7);

            result           .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 7))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 5))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 1033))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 1/60f))
                .andThen(new PermanentChase());
        }
        else
        {
            result = new StateTransition(Ghost.Mode.SCATTER, 5);
            result
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 5))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 20))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 5))
                .andThen(new StateTransition(Ghost.Mode.CHASING, 1037))
                .andThen(new StateTransition(Ghost.Mode.SCATTER, 1 / 60f))
                .andThen(new PermanentChase());
        }
        return result;
    }
}
