package main;

import fileio.CardInput;
import fileio.GameInput;
import fileio.DecksInput;

import java.util.ArrayList;

public class Session {
    private Player[] players = new Player[2];
    private Game game;
    private int noWinsPlayer1 = 0;
    private int noWinsPlayer2 = 0;

    public int getNoWinsPlayer1() {
        return noWinsPlayer1;
    }

    public void setNoWinsPlayer1(int noWinsPlayer1) {
        this.noWinsPlayer1 = noWinsPlayer1;
    }

    public int getNoWinsPlayer2() {
        return noWinsPlayer2;
    }

    public void setNoWinsPlayer2(int noWinsPlayer2) {
        this.noWinsPlayer2 = noWinsPlayer2;
    }

    public Session(DecksInput inputPlayer1Decks, DecksInput inputPlayer2Decks, ArrayList<GameInput> games) {
        ArrayList<ArrayList<Card>> player1Decks = new ArrayList<ArrayList<Card>>();
        for (ArrayList<CardInput> deck : inputPlayer1Decks.getDecks()) {
            player1Decks.add(new ArrayList<Card>());
            for (CardInput cardInput : deck)
                player1Decks.getLast().add(new Card(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
        }
        players[0] = new Player(inputPlayer1Decks.getNrDecks(), inputPlayer1Decks.getNrCardsInDeck(), player1Decks);

        ArrayList<ArrayList<Card>> player2Decks = new ArrayList<ArrayList<Card>>();
        for (ArrayList<CardInput> deck : inputPlayer2Decks.getDecks()) {
            player2Decks.add(new ArrayList<Card>());
            for (CardInput cardInput : deck)
                player2Decks.getLast().add(new Card(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
        }
        players[1] = new Player(inputPlayer1Decks.getNrDecks(), inputPlayer1Decks.getNrCardsInDeck(), player2Decks);

        for (GameInput gameInput : games) {
            Card player1HeroCard = new Card(gameInput.getStartGame().getPlayerOneHero().getHealth(),
                    gameInput.getStartGame().getPlayerOneHero().getMana(),
                    gameInput.getStartGame().getPlayerOneHero().getAttackDamage(),
                    gameInput.getStartGame().getPlayerOneHero().getDescription(),
                    gameInput.getStartGame().getPlayerOneHero().getColors(),
                    gameInput.getStartGame().getPlayerOneHero().getName());
            Card player2HeroCard = new Card(gameInput.getStartGame().getPlayerTwoHero().getHealth(),
                    gameInput.getStartGame().getPlayerTwoHero().getMana(),
                    gameInput.getStartGame().getPlayerTwoHero().getAttackDamage(),
                    gameInput.getStartGame().getPlayerTwoHero().getDescription(),
                    gameInput.getStartGame().getPlayerTwoHero().getColors(),
                    gameInput.getStartGame().getPlayerTwoHero().getName());
            Game game = new Game(gameInput.getStartGame().getShuffleSeed(),
                    gameInput.getStartGame().getStartingPlayer(),
                    player1Decks.get(gameInput.getStartGame().getPlayerOneDeckIdx()),
                    player2Decks.get(gameInput.getStartGame().getPlayerTwoDeckIdx()),
                    player1HeroCard,
                    player2HeroCard);
        }
    }
}
