package de.codesourcery.jpacman;

import java.awt.Point;

public class Player extends Entity
{
    public int score;

    public void reset() {
        score = 0;
        isMoving = false;
    }
}