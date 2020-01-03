package de.codesourcery.jpacman;

public class DotCounter
{
    public int dotsEaten;
    private int limit;
    public boolean limitReached;
    public Runnable limitAction;

    public DotCounter() {
    }

    public DotCounter(Runnable limitAction) {
        this.limitAction = limitAction;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        this.limitReached = false;
    }

    public void dotEaten()
    {
        dotsEaten++;
        if ( dotsEaten >= limit && ! limitReached )
        {
            limitReached=true;
            if ( limitAction != null )
            {
                limitAction.run();
            }
        }
    }

    public void reset() {
        dotsEaten = 0;
        limitReached = false;
    }
}
