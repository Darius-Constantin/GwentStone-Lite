package game;

import cards.Card;
import cards.MinionCard;
import lombok.Getter;

import java.util.ArrayList;

public class Player {
    final private int noDecks;
    final private int noCardsInDeck;
    @Getter
    final private ArrayList<ArrayList<MinionCard>> decks;

    public Player(int noDecks, int noCardsInDeck, ArrayList<ArrayList<MinionCard>> decks) {
        this.noDecks = noDecks;
        this.noCardsInDeck = noCardsInDeck;
        this.decks = decks;
    }

}
