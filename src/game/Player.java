package game;

import cards.Card;
import cards.MinionCard;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Player {
    final private int noDecks;
    final private int noCardsInDeck;
    @Getter
    final private ArrayList<ArrayList<MinionCard>> decks;
    @Getter
    private int noOfWins = 0;

    public void addWin() {
        noOfWins++;
    }

    public Player(int noDecks, int noCardsInDeck, ArrayList<ArrayList<MinionCard>> decks) {
        this.noDecks = noDecks;
        this.noCardsInDeck = noCardsInDeck;
        this.decks = decks;
    }

}
