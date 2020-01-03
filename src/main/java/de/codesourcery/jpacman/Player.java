package de.codesourcery.jpacman;

public class Player extends Entity
{
    public int score;

    public long startTimeCanEatGhosts;
    public boolean canEatGhosts;
    public int lifes;
    public boolean bonusLifeAwarded;

    public void reset()
    {
        lifes = 3;
        bonusLifeAwarded = false;
        startTimeCanEatGhosts = 0;
        canEatGhosts = false;
        score = 0;
        isMoving = false;
    }
}