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

public final class Session {
    private static final HashMap<String, Function<CardInput, MinionCard>> MINION_CARD_CONSTRUCTOR =
            new HashMap<>();
    private static final HashMap<String, Function<CardInput, HeroCard>> HERO_CONSTRUCTOR =
            new HashMap<>();

    static {
        MINION_CARD_CONSTRUCTOR.put("Sentinel",
                (CardInput input) -> new Sentinel(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("Berserker",
                (CardInput input) -> new Berserker(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("Goliath",
                (CardInput input) -> new Goliath(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("Warden",
                (CardInput input) -> new Warden(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("The Ripper",
                (CardInput input) -> new TheRipper(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("Miraj",
                (CardInput input) -> new Miraj(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("The Cursed One",
                (CardInput input) -> new TheCursedOne(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));
        MINION_CARD_CONSTRUCTOR.put("Disciple",
                (CardInput input) -> new Disciple(input.getHealth(), input.getMana(),
                        input.getAttackDamage(), input.getDescription(), input.getColors(),
                        input.getName()));

        HERO_CONSTRUCTOR.put("Lord Royce",
                (CardInput input) -> new LordRoyce(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()));
        HERO_CONSTRUCTOR.put("Empress Thorina",
                (CardInput input) -> new EmpressThorina(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()));
        HERO_CONSTRUCTOR.put("King Mudface",
                (CardInput input) -> new KingMudface(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()));
        HERO_CONSTRUCTOR.put("General Kocioraw",
                (CardInput input) -> new GeneralKocioraw(input.getMana(),
                        input.getDescription(), input.getColors(), input.getName()));
    }

    private final Player[] players = new Player[2];
    private final ArrayList<GameInput> gamesInput;

    /**
     * Constructor to begin a Gwentstone session consisting of one or multiple games between
     * players with multiple possible decks.
     *
     * @param inputPlayer1Decks The available decks of the first player.
     * @param inputPlayer2Decks The available decks of the second player.
     * @param gamesInput        The input of the games which follow to be played.
     */
    public Session(final DecksInput inputPlayer1Decks, final DecksInput inputPlayer2Decks,
                   final ArrayList<GameInput> gamesInput) {
        ArrayList<ArrayList<MinionCard>> player1Decks = new ArrayList<>();
        for (ArrayList<CardInput> deck : inputPlayer1Decks.getDecks()) {
            player1Decks.add(new ArrayList<>());
            for (CardInput cardInput : deck) {
                player1Decks.getLast().add(MINION_CARD_CONSTRUCTOR.get(cardInput.getName())
                        .apply(cardInput));
            }
        }
        players[0] = new Player(player1Decks);

        ArrayList<ArrayList<MinionCard>> player2Decks = new ArrayList<>();
        for (ArrayList<CardInput> deck : inputPlayer2Decks.getDecks()) {
            player2Decks.add(new ArrayList<>());
            for (CardInput cardInput : deck) {
                player2Decks.getLast().add(MINION_CARD_CONSTRUCTOR.get(cardInput.getName())
                        .apply(cardInput));
            }
        }
        players[1] = new Player(player2Decks);
        this.gamesInput = gamesInput;
    }

    /**
     * Upon the win of a player by the death of the enemy, use this function to increment
     * @param playerIdx The index of the player whose wins counter will be incremented.
     */
    public void incrementPlayerWins(final int playerIdx) {
        players[playerIdx].addWin();
    }

    /**
     * Function used to retrieve the wins counter of a player.
     * @param playerIdx The index of the player whose wins counter is requested.
     * @return The number of wins for the supplied player.
     */
    public int getPlayerWins(final int playerIdx) {
        return players[playerIdx].getNoOfWins();
    }

    /**
     * Once a session has been established, call this function to begin playing each game
     * sequentially. In between constructing the session and beginning it, additional set up can
     * be done if required.
     */
    public void beginSession() {
        for (GameInput gameInput : gamesInput) {
            CardInput tmpHero = gameInput.getStartGame().getPlayerOneHero();
            Card player1HeroCard = HERO_CONSTRUCTOR.get(tmpHero.getName()).apply(tmpHero);

            tmpHero = gameInput.getStartGame().getPlayerTwoHero();
            Card player2HeroCard = HERO_CONSTRUCTOR.get(tmpHero.getName()).apply(tmpHero);

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
