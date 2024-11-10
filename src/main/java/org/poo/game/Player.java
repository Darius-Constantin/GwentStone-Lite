package org.poo.game;

import org.poo.cards.MinionCard;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class Player {
    private final ArrayList<ArrayList<MinionCard>> decks;
    private int noOfWins = 0;

    public void addWin() {
        noOfWins++;
    }

    public Player(final ArrayList<ArrayList<MinionCard>> decks) {
        this.decks = decks;
    }

}
