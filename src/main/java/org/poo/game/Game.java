package org.poo.game;

import lombok.Getter;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.Coordinates;
import org.poo.fileio.IOHandler;
import org.poo.game.cards.Card;
import org.poo.game.cards.CardType;
import org.poo.game.cards.MinionCard;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Hero;
import org.poo.game.entities.Minion;
import org.poo.game.entities.SpecialMinion;
import org.poo.metaGame.Session;
import org.poo.utils.Quintet;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.function.Function;

public final class Game {
    public static final int TABLE_HEIGHT = 4;
    public static final int TABLE_WIDTH = 5;
    public static final int P1_BACK = 3;
    public static final int P1_FRONT = 2;
    public static final int P2_FRONT = 1;
    public static final int P2_BACK = 0;
    public static final int MAX_MANA_GAIN = 10;
    public static final int MIN_MANA_GAIN = 1;
    private static final HashMap<String, Command> COMMANDS_MAP = new HashMap<>();
    private static final HashMap<String,
            Function<Quintet<MinionCard, Integer, Integer, Game, Integer>, Minion>> MINION_CONSTRUCTOR =
            new HashMap<>();

    static {
        MINION_CONSTRUCTOR.put("Disciple",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new SpecialMinion((SpecialMinionCard) params.obj0(), params.obj1(),
                                params.obj2(), params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("The Ripper",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new SpecialMinion((SpecialMinionCard) params.obj0(), params.obj1(),
                                params.obj2(), params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("Miraj",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new SpecialMinion((SpecialMinionCard) params.obj0(), params.obj1(),
                                params.obj2(), params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("The Cursed One",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new SpecialMinion((SpecialMinionCard) params.obj0(), params.obj1(),
                                params.obj2(), params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("Goliath",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new Minion(params.obj0(), params.obj1(), params.obj2(),
                                params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("Warden",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new Minion(params.obj0(), params.obj1(), params.obj2(),
                                params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("Sentinel",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new Minion(params.obj0(), params.obj1(), params.obj2(),
                                params.obj3(), params.obj4()));
        MINION_CONSTRUCTOR.put("Berserker",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) ->
                        new Minion(params.obj0(), params.obj1(), params.obj2(),
                                params.obj3(), params.obj4()));

        for (Command value : Command.values()) {
            COMMANDS_MAP.put(value.command, value);
        }
    }

    private final Session currentSession;
    private final int startingPlayer;
    private final Hero[] heroes = new Hero[2];
    private final ArrayList<ActionsInput> actions;
    @Getter
    private final Minion[][] playedCards = new Minion[TABLE_HEIGHT][TABLE_WIDTH];
    @Getter
    private int totalTurns = 0;
    @Getter
    private boolean isGameOver = false;

    /**
     * Constructor for beginning a game, designed to be called inside a session.
     *
     * @param currentSession  Session which constructed the game.
     * @param seed            The Random seed to be used for shuffling decks.
     * @param startingPlayer  The index (0 or 1) of the player who acts first.
     * @param player1Deck     The selected deck of Player 1.
     * @param player2Deck     The selected deck of Player 2.
     * @param player1HeroCard The hero used by Player 1.
     * @param player2HeroCard The hero used by Player 2.
     * @param actions         A list of action each player takes sequentially until the end of the game.
     */
    public Game(final Session currentSession, final int seed, final int startingPlayer,
                final ArrayList<MinionCard> player1Deck, final ArrayList<MinionCard> player2Deck,
                final Card player1HeroCard, final Card player2HeroCard,
                final ArrayList<ActionsInput> actions) {
        this.currentSession = currentSession;
        this.startingPlayer = startingPlayer;
        heroes[0] = new Hero(player1HeroCard,
                shuffleDeck(player1Deck, seed), this, 0);
        heroes[1] = new Hero(player2HeroCard,
                shuffleDeck(player2Deck, seed), this, 1);
        this.actions = actions;
    }

    /**
     * Function used to shuffle a list of cards by shallow-copying an {@code ArrayList} of
     * {@link MinionCard} whose order of elements is then randomized as dictated by the supplied
     * seed. A shuffled list of cards is a deck.
     * @param cardList The list of cards that will be shuffled.
     * @param seed The seed by which the cards will be shuffled.
     * @return The resulting shuffled deck.
     */
    public static ArrayList<MinionCard> shuffleDeck(final ArrayList<MinionCard> cardList,
                                                    final int seed) {
        ArrayList<MinionCard> deck = new ArrayList<>(cardList);
        Collections.shuffle(deck, new Random(seed));
        return deck;
    }

    /**
     * Once a game has been established, call this function to begin playing it. In between
     * constructing the game and playing it, additional set up can be done if required. Once playing
     * commences, each hero receives {@value MIN_MANA_GAIN} mana, and the game's actions are looped
     * over. If the game ends by the death of a hero, yet there are actions left to be done,
     * {@link CommandType#GAMEPLAY} actions will be ignored.
     */
    public void playGame() {
        for (int i = 0; i < 2; i++) {
            heroes[i].reset(MIN_MANA_GAIN);
        }

        for (ActionsInput actionInput : actions) {
            Command command = COMMANDS_MAP.get(actionInput.getCommand());
            if (command == null || (isGameOver && command.getType() == CommandType.GAMEPLAY)) {
                continue;
            }
            IOHandler.getInstance().beginObject();
            command.execute(this, actionInput);
            IOHandler.getInstance().endObject();
        }
    }

    /**
     * If a turn is considered 2 consecutive end turns (1 from each player), then this function
     * calculates the current turn starting from 1.
     * @return The current turn as described above.
     */
    public int getCurrentTurn() {
        return totalTurns / 2 + 1;
    }

    /**
     * Function used to retrieve the player index who will act during the current turn,
     * @return The player index (0 or 1) of the current player.
     */
    public int getCurrentPlayerIdx() {
        return totalTurns % 2 == 0 ? startingPlayer : (startingPlayer == 0 ? 1 : 0);
    }

    /**
     * Function called by any minion on the table after its death to update the position of
     * minions to the right of it.
     *
     * @param minion The minion that has died.
     */
    public void onMinionDeath(final Minion minion) {
        for (int i = minion.getY(); i < TABLE_WIDTH - 1; i++) {
            playedCards[minion.getX()][i] = playedCards[minion.getX()][i + 1];
            if (playedCards[minion.getX()][i] == null) {
                break;
            }
            playedCards[minion.getX()][i].setY(i);
        }
        playedCards[minion.getX()][TABLE_WIDTH - 1] = null;
    }

    /**
     * Function called by any hero after its death to update the state of the game (mark it as
     * over). There is no need to pass the hero as an argument, because never does it happen that
     * a player will kill their hero, thus it is always the opposite hero of the current player that
     * dies.
     */
    public void onHeroDeath() {
        IOHandler.getInstance().beginObject();
        IOHandler.getInstance().writeToObject("gameEnded",
                "Player " + (getCurrentPlayerIdx() == 0 ? "one" : "two")
                        + " killed the enemy hero.");
        IOHandler.getInstance().writeObjectToOutput();
        IOHandler.getInstance().endObject();
        currentSession.incrementPlayerWins(getCurrentPlayerIdx());
        isGameOver = true;
    }

    /**
     * A function for determining whether there is an active {@link CardType#TAUNT}-type entity on
     * the enemy's side of the board.
     *
     * @return True if a taunt is present on the enemy's side. False otherwise.
     */
    public boolean isTauntOnEnemySide() {
        for (var minion : playedCards[getCurrentPlayerIdx() + 1]) {
            if (minion == null) {
                break;
            }
            if (minion.getCard().getType() == CardType.TAUNT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enum that classifies types of commands. Mostly useful to differentiate between gameplay
     * and non-gameplay commands. As per the
     * <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html">official
     * documentation</a>, it is not defined in its own file for increased encapsulation and
     * maintainability.
     * <p>
     * {@link #GAMEPLAY}
     * {@link #DEBUG}
     * {@link #STATS}
     */
    private enum CommandType {
        GAMEPLAY,
        DEBUG,
        STATS,
    }

    /**
     * Enum used to associate to a string command a {@link CommandType} and an execution function
     * significant to that command.
     */
    @Getter
    private enum Command implements Action {
        PLACE_CARD("placeCard", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                MinionCard cardToPlace = game.heroes[currentPlayerIdx].getCard(action.getHandIdx());
                if (cardToPlace == null) {
                    return;
                }

                IOHandler.getInstance().writeToObject("command", "placeCard");

                if (cardToPlace.getMana() > game.heroes[currentPlayerIdx].getMana()) {
                    IOHandler.getInstance().writeToObject("command", "placeCard");
                    IOHandler.getInstance().writeToObject("error",
                            "Not enough mana to place card on table.");
                    IOHandler.getInstance().writeToObject("handIdx", action.getHandIdx());
                    IOHandler.getInstance().writeObjectToOutput();
                    return;
                }

                int x = (cardToPlace.getType() == CardType.GENERIC
                        || cardToPlace.getType() == CardType.SPECIAL_BACKLINE)
                        ? (currentPlayerIdx == 0 ? P1_BACK : P2_BACK)
                        : (currentPlayerIdx == 0 ? P1_FRONT : P2_FRONT);
                int y = 0;

                for (int i = 0; i < TABLE_HEIGHT; i++) {
                    if (game.playedCards[x][i] == null) {
                        break;
                    }
                    y++;
                }

                if (y > TABLE_WIDTH - 1) {
                    IOHandler.getInstance().writeToObject("error",
                            "Cannot place card on table since row is full.");
                    IOHandler.getInstance().writeObjectToOutput();
                    return;
                }

                Minion minion =
                        MINION_CONSTRUCTOR.get(cardToPlace.getName())
                                .apply(new Quintet<>(cardToPlace, x, y, game, currentPlayerIdx));
                game.heroes[currentPlayerIdx].addMana(-cardToPlace.getMana());
                game.playedCards[x][y] = minion;
                game.heroes[currentPlayerIdx].removeCard(action.getHandIdx());
            }
        },
        CARD_USES_ATTACK("cardUsesAttack", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                Coordinates cardAttacker = action.getCardAttacker();
                Coordinates cardAttacked = action.getCardAttacked();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                Minion target = game.playedCards[cardAttacked.getX()][cardAttacked.getY()];

                if (attacker == null || target == null) {
                    return;
                }

                IOHandler.getInstance().writeToObject("command", "cardUsesAttack");
                IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                        IOHandler.getInstance()
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.getInstance().writeJsonNodeToObject("cardAttacked",
                        IOHandler.getInstance()
                                .createNodeFromObject(action.getCardAttacked()));

                attacker.attack(target);
            }
        },
        CARD_USES_ABILITY("cardUsesAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                Coordinates cardAttacker = action.getCardAttacker();
                Coordinates cardAttacked = action.getCardAttacked();

                if (game.playedCards[cardAttacker.getX()][cardAttacker.getY()] == null) {
                    return;
                }

                CardType casterType =
                        game.playedCards[cardAttacker.getX()][cardAttacker.getY()]
                                .getCard()
                                .getType();


                if ((casterType.getAttributes() & CardType.SPECIAL) == 0b00000000) {
                    return;
                }

                SpecialMinion caster =
                        (SpecialMinion) game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                Minion target = game.playedCards[cardAttacked.getX()][cardAttacked.getY()];

                if (target == null) {
                    return;
                }

                IOHandler.getInstance().writeToObject("command", "cardUsesAbility");
                IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                        IOHandler.getInstance()
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.getInstance().writeJsonNodeToObject("cardAttacked",
                        IOHandler.getInstance()
                                .createNodeFromObject(action.getCardAttacked()));

                caster.useAbility(target);
            }
        },
        USE_ATTACK_HERO("useAttackHero", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                Coordinates cardAttacker = action.getCardAttacker();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];

                if (attacker == null) {
                    return;
                }

                IOHandler.getInstance().writeToObject("command", "useAttackHero");
                IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                        IOHandler.getInstance()
                                .createNodeFromObject(action.getCardAttacker()));

                attacker.attack(game.heroes[currentPlayerIdx == 0 ? 1 : 0]);
            }
        },
        USE_HERO_ABILITY("useHeroAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());

                game.heroes[currentPlayerIdx].useAbility(game.playedCards[action.getAffectedRow()],
                        action.getAffectedRow());
            }
        },
        END_PLAYER_TURN("endPlayerTurn", CommandType.GAMEPLAY) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                game.heroes[currentPlayerIdx].setCanAct(true);
                for (int i = currentPlayerIdx == 0 ? P1_FRONT : P2_BACK;
                     i <= (currentPlayerIdx == 0 ? P1_BACK : P2_FRONT); i++) {
                    for (Minion minion : game.playedCards[i]) {
                        if (minion == null) {
                            break;
                        }
                        minion.reset();
                    }
                }

                if (currentPlayerIdx != game.startingPlayer) {
                    for (int i = 0; i < 2; i++) {
                        game.heroes[i].reset(Math.min(game.getCurrentTurn() + 1, MAX_MANA_GAIN));
                    }
                }
                game.totalTurns++;
            }
        },
        GET_PLAYER_DECK("getPlayerDeck", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                Hero selectedHero = game.heroes[action.getPlayerIdx() - 1];
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.getInstance().writeJsonNodeToObject("output",
                        IOHandler.getInstance()
                                .createNodeFromObject(selectedHero.getAvailableCards()));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_PLAYER_HERO("getPlayerHero", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                Hero selectedHero = game.heroes[action.getPlayerIdx() - 1];
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.getInstance().writeJsonNodeToObject("output",
                        IOHandler.getInstance().createNodeFromObject(selectedHero));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_PLAYER_TURN("getPlayerTurn", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("output", game.getCurrentPlayerIdx() + 1);
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_FROZEN_CARDS_ON_TABLE("getFrozenCardsOnTable", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                ArrayList<Minion> frozenMinions = new ArrayList<>(TABLE_HEIGHT * TABLE_WIDTH);
                for (Minion[] line : game.playedCards) {
                    frozenMinions.addAll(new ArrayList<>(Arrays.asList(line)));
                }
                frozenMinions.removeIf(minion -> (minion == null || !minion.isFrozen()));
                IOHandler.getInstance().writeJsonNodeToObject("output",
                        IOHandler.getInstance().createNodeFromObject(frozenMinions));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_PLAYER_MANA("getPlayerMana", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.getInstance().writeToObject("output",
                        game.heroes[action.getPlayerIdx() - 1].getMana());
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_CARD_AT_POSITION("getCardAtPosition", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("x", action.getX());
                IOHandler.getInstance().writeToObject("y", action.getY());
                Minion minion = game.playedCards[action.getX()][action.getY()];
                if (minion == null) {
                    IOHandler.getInstance().writeToObject("output",
                            "No card available at that position.");
                } else {
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance().createNodeFromObject(minion));
                }
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_CARDS_ON_TABLE("getCardsOnTable", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                ArrayList<ArrayList<Minion>> table = new ArrayList<>(TABLE_HEIGHT);
                for (Minion[] line : game.playedCards) {
                    table.add(new ArrayList<>(Arrays.asList(line)));
                    table.getLast().removeIf(Objects::isNull);
                }
                IOHandler.getInstance().writeJsonNodeToObject("output",
                        IOHandler.getInstance().createNodeFromObject(table));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_CARDS_IN_HAND("getCardsInHand", CommandType.DEBUG) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.getInstance().writeJsonNodeToObject("output",
                        IOHandler.getInstance().createNodeFromObject(game
                                .heroes[action.getPlayerIdx() - 1].getInHandCards()));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_TOTAL_GAMES_PLAYER("getTotalGamesPlayed", CommandType.STATS) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("output",
                        game.currentSession.getPlayerWins(0)
                                + game.currentSession.getPlayerWins(1));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_PLAYER_ONE_WINS("getPlayerOneWins", CommandType.STATS) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("output",
                        game.currentSession.getPlayerWins(0));
                IOHandler.getInstance().writeObjectToOutput();
            }
        },
        GET_PLAYER_TWO_WINS("getPlayerTwoWins", CommandType.STATS) {
            @Override
            public void execute(final Game game, final ActionsInput action) {
                IOHandler.getInstance().writeToObject("command", action.getCommand());
                IOHandler.getInstance().writeToObject("output",
                        game.currentSession.getPlayerWins(1));
                IOHandler.getInstance().writeObjectToOutput();
            }
        };

        private final String command;
        private final CommandType type;

        Command(final String command, final CommandType type) {
            this.command = command;
            this.type = type;
        }
    }

    private interface Action {
        void execute(Game game, ActionsInput actionInput);
    }
}
