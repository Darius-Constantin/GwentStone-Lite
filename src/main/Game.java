package main;

import java.util.ArrayList;

public class Game {
    public final int seed;
    public final int startingPlayer;
    private final Hero[] heroes = new Hero[2];

    public Game(int seed, int startingPlayer, ArrayList<Card> player1Deck, ArrayList<Card> player2Deck,
                Card player1HeroCard, Card player2HeroCard) {
        this.seed = seed;
        this.startingPlayer = startingPlayer;
        heroes[0] = new Hero(player1HeroCard, Card.shuffleDeck(Card.cloneDeck(player1Deck), seed));
        heroes[1] = new Hero(player2HeroCard, Card.shuffleDeck(Card.cloneDeck(player2Deck), seed));
    }
}
