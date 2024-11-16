package org.poo.metaGame;

import org.poo.game.cards.MinionCard;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class Player {
    private final ArrayList<ArrayList<MinionCard>> decks;
    private int noOfWins = 0;

    /**
     * Function used to increment the {@link #noOfWins} for this player.
     */
    public void addWin() {
        noOfWins++;
    }

    public Player(final ArrayList<ArrayList<MinionCard>> decks) {
        this.decks = decks;
    }
}
