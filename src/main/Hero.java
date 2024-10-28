package main;

import java.util.ArrayList;

public class Hero {
    private ArrayList<Card> availableCards;
    private ArrayList<Card> inHandCards;
    private Card[][] playedCards = new Card[2][5];
    private Card hero;
    private int mana = 0;

    public Hero(Card hero, ArrayList<Card> deck) {
        this.hero = hero;
        this.availableCards = deck;
        this.inHandCards = new ArrayList<>(deck.size());
    }
}
