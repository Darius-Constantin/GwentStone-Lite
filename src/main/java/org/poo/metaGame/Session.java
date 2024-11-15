package org.poo.metaGame;

import org.poo.game.cards.Card;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionCard;
import org.poo.fileio.CardInput;
import org.poo.fileio.GameInput;
import org.poo.fileio.DecksInput;
import org.poo.game.Game;
import org.poo.game.cards.genericCards.Berserker;
import org.poo.game.cards.genericCards.Goliath;
import org.poo.game.cards.genericCards.Sentinel;
import org.poo.game.cards.genericCards.Warden;
import org.poo.game.cards.heroCards.EmpressThorina;
import org.poo.game.cards.heroCards.GeneralKocioraw;
import org.poo.game.cards.heroCards.KingMudface;
import org.poo.game.cards.heroCards.LordRoyce;
import org.poo.game.cards.specialCards.Disciple;
import org.poo.game.cards.specialCards.Miraj;
import org.poo.game.cards.specialCards.TheCursedOne;
import org.poo.game.cards.specialCards.TheRipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class Session {
    private final Player[] players = new Player[2];
    private final ArrayList<GameInput> gamesInput;

    private final static HashMap<String, Function<CardInput, MinionCard>> minionCardConstructor =
            new HashMap<>();

    private final static HashMap<String, Function<CardInput, HeroCard>> heroConstructor =
            new HashMap<>();

    static {
        minionCardConstructor.put("Sentinel",
                (CardInput input) -> { return new Sentinel(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("Berserker",
                (CardInput input) -> { return new Berserker(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("Goliath",
                (CardInput input) -> { return new Goliath(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("Warden",
                (CardInput input) -> { return new Warden(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("The Ripper",
                (CardInput input) -> { return new TheRipper(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("Miraj",
                (CardInput input) -> { return new Miraj(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("The Cursed One",
                (CardInput input) -> { return new TheCursedOne(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });
        minionCardConstructor.put("Disciple",
                (CardInput input) -> { return new Disciple(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()); });

        heroConstructor.put("Lord Royce",
                (CardInput input) -> { return new LordRoyce(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()); });
        heroConstructor.put("Empress Thorina",
                (CardInput input) -> { return new EmpressThorina(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()); });
        heroConstructor.put("King Mudface",
                (CardInput input) -> { return new KingMudface(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()); });
        heroConstructor.put("General Kocioraw",
                (CardInput input) -> { return new GeneralKocioraw(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()); });
    }

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
                player1Decks.getLast().add(minionCardConstructor.get(cardInput.getName()).apply(cardInput));
            }
        }
        players[0] = new Player(player1Decks);

        ArrayList<ArrayList<MinionCard>> player2Decks = new ArrayList<>();
        for (ArrayList<CardInput> deck : inputPlayer2Decks.getDecks()) {
            player2Decks.add(new ArrayList<>());
            for (CardInput cardInput : deck) {
                player2Decks.getLast().add(minionCardConstructor.get(cardInput.getName()).apply(cardInput));
            }
        }
        players[1] = new Player(player2Decks);
        this.gamesInput = gamesInput;
    }

    public void beginSession() throws IllegalAccessException {
        for (GameInput gameInput : gamesInput) {
            CardInput tmpHero = gameInput.getStartGame().getPlayerOneHero();
            Card player1HeroCard = heroConstructor.get(tmpHero.getName()).apply(tmpHero);

            tmpHero = gameInput.getStartGame().getPlayerTwoHero();
            Card player2HeroCard = heroConstructor.get(tmpHero.getName()).apply(tmpHero);

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
