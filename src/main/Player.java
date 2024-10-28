package main;

import java.util.ArrayList;

public class Player {
    final private int noDecks;
    final private int noCardsInDeck;
    final private ArrayList<ArrayList<Card>> decks;

    public Player(int noDecks, int noCardsInDeck, ArrayList<ArrayList<Card>> decks) {
        this.noDecks = noDecks;
        this.noCardsInDeck = noCardsInDeck;
        this.decks = decks;
    }
}
