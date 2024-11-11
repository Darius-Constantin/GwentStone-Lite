package org.poo.meta_game;

import org.poo.game.cards.Card;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionCard;
import org.poo.fileio.CardInput;
import org.poo.fileio.GameInput;
import org.poo.fileio.DecksInput;
import org.poo.game.Game;

import java.util.ArrayList;

public class Session {
    private final Player[] players = new Player[2];
    private final ArrayList<GameInput> gamesInput;

    public void addPlayerWin(final int playerIdx) {
        players[playerIdx].addWin();
    }

    public int getPlayerWins(final int playerIdx) {
        return players[playerIdx].getNoOfWins();
    }

    public Session(final DecksInput inputPlayer1Decks,
                   final DecksInput inputPlayer2Decks,
                   final ArrayList<GameInput> gamesInput) {
        ArrayList<ArrayList<MinionCard>> player1Decks = new ArrayList<>();
        for (ArrayList<CardInput> deck : inputPlayer1Decks.getDecks()) {
            player1Decks.add(new ArrayList<>());
            for (CardInput cardInput : deck) {
                player1Decks.getLast().add(new MinionCard(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
            }
        }
        players[0] = new Player(player1Decks);

        ArrayList<ArrayList<MinionCard>> player2Decks = new ArrayList<>();
        for (ArrayList<CardInput> deck : inputPlayer2Decks.getDecks()) {
            player2Decks.add(new ArrayList<>());
            for (CardInput cardInput : deck) {
                player2Decks.getLast().add(new MinionCard(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
            }
        }
        players[1] = new Player(player2Decks);
        this.gamesInput = gamesInput;
    }

    public void beginSession() throws IllegalAccessException {
        for (GameInput gameInput : gamesInput) {
            CardInput tmpHero = gameInput.getStartGame().getPlayerOneHero();
            Card player1HeroCard = new HeroCard(tmpHero.getMana(), tmpHero.getDescription(),
                    tmpHero.getColors(), tmpHero.getName());

            tmpHero = gameInput.getStartGame().getPlayerTwoHero();
            Card player2HeroCard = new HeroCard(tmpHero.getMana(), tmpHero.getDescription(),
                    tmpHero.getColors(), tmpHero.getName());

            Game currentGame = new Game(this,
                    gameInput.getStartGame().getShuffleSeed(),
                    gameInput.getStartGame().getStartingPlayer() - 1,
                    players[0].getDecks().get(gameInput.getStartGame().getPlayerOneDeckIdx()),
                    players[1].getDecks().get(gameInput.getStartGame().getPlayerTwoDeckIdx()),
                    player1HeroCard, player2HeroCard, gameInput.getActions());

            currentGame.playGame();
        }
    }
}
