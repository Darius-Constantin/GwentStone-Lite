package org.poo.game;

import org.poo.fileio.CardInput;
import org.poo.fileio.Coordinates;
import org.poo.game.cards.Card;
import org.poo.game.cards.CardType;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionCard;
import org.poo.game.cards.SpecialMinionCard;
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
import org.poo.game.entities.Hero;
import org.poo.game.entities.Entity;
import org.poo.game.entities.Minion;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.IOHandler;
import lombok.Getter;
import org.poo.game.entities.SpecialMinion;
import org.poo.metaGame.Session;
import org.poo.utils.Quintet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

public class Game {
    private final Session currentSession;
    private final int startingPlayer;
    private final Hero[] heroes = new Hero[2];
    private final ArrayList<ActionsInput> actions;

    public static final int TABLE_HEIGHT = 4;
    public static final int TABLE_WIDTH = 5;
    public static final int P1_BACK = 3;
    public static final int P1_FRONT = 2;
    public static final int P2_FRONT = 1;
    public static final int P2_BACK = 0;
    public static final int MAX_MANA_GAIN = 10;

    @Getter
    private final Minion[][] playedCards = new Minion[TABLE_HEIGHT][TABLE_WIDTH];
    @Getter
    private int totalTurns = 0;
    @Getter
    private boolean isGameOver = false;

    private interface Action {
        void execute(Game game, ActionsInput actionInput) throws IllegalAccessException;
    }

    private enum CommandType {
        GAMEPLAY,
        DEBUG,
        STATS,
    }

    @Getter
    private enum Command implements Action {
        PLACE_CARD("placeCard", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                MinionCard cardToPlace = game.heroes[currentPlayerIdx].getCard(action.getHandIdx());
                if (cardToPlace == null) {
                    return;
                }

                IOHandler.INSTANCE.writeToObject("command", "placeCard");

                if (cardToPlace.getMana() > game.heroes[currentPlayerIdx].getMana()) {
                    IOHandler.INSTANCE.writeToObject("command", "placeCard");
                    IOHandler.INSTANCE.writeToObject("error",
                            "Not enough mana to place card on table.");
                    IOHandler.INSTANCE.writeToObject("handIdx", action.getHandIdx());
                    IOHandler.INSTANCE.writeObjectToOutput();
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
                    IOHandler.INSTANCE.writeToObject("error",
                            "Cannot place card on table since row is full.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                Minion minion =
                        minionConstructor.get(cardToPlace.getName())
                                .apply(new Quintet<>(cardToPlace, x, y, game, currentPlayerIdx));
                game.heroes[currentPlayerIdx].addMana(-cardToPlace.getMana());
                game.playedCards[x][y] = minion;
                game.heroes[currentPlayerIdx].removeCard(action.getHandIdx());
            }
        },
        CARD_USES_ATTACK("cardUsesAttack", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
                Coordinates cardAttacker = action.getCardAttacker();
                Coordinates cardAttacked = action.getCardAttacked();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                Minion target = game.playedCards[cardAttacked.getX()][cardAttacked.getY()];

                if (attacker == null || target == null) {
                    return;
                }

                IOHandler.INSTANCE.writeToObject("command", "cardUsesAttack");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacked()));

                attacker.attack(target);
            }
        },
        CARD_USES_ABILITY("cardUsesAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
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

                IOHandler.INSTANCE.writeToObject("command", "cardUsesAbility");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacked()));

                caster.useAbility(target);
            }
        },
        USE_ATTACK_HERO("useAttackHero", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                Coordinates cardAttacker = action.getCardAttacker();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];

                if (attacker == null) {
                    return;
                }

                IOHandler.INSTANCE.writeToObject("command", "useAttackHero");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));

                attacker.attack(game.heroes[currentPlayerIdx == 0 ? 1 : 0]);
            }
        },
        USE_HERO_ABILITY("useHeroAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                IOHandler.INSTANCE.writeToObject("command", "useHeroAbility");
                IOHandler.INSTANCE.writeToObject("affectedRow", action.getAffectedRow());

                game.heroes[currentPlayerIdx].useAbility(game.playedCards[action.getAffectedRow()],
                        action.getAffectedRow());
            }
        },
        END_PLAYER_TURN("endPlayerTurn", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
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
            public void execute(Game game, ActionsInput action) {
                Hero selectedHero = game.heroes[action.getPlayerIdx() - 1];
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(selectedHero.getAvailableCards()));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_PLAYER_HERO("getPlayerHero", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                Hero selectedHero = game.heroes[action.getPlayerIdx() - 1];
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(selectedHero));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_PLAYER_TURN("getPlayerTurn", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("output", game.getCurrentPlayerIdx() + 1);
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_FROZEN_CARDS_ON_TABLE("getFrozenCardsOnTable", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                ArrayList<Minion> frozenMinions = new ArrayList<>(TABLE_HEIGHT * TABLE_WIDTH);
                for (Minion[] line : game.playedCards) {
                    frozenMinions.addAll(new ArrayList<>(Arrays.asList(line)));
                }
                frozenMinions.removeIf(minion -> (minion == null || !minion.isFrozen()));
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(frozenMinions));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_PLAYER_MANA("getPlayerMana", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.INSTANCE.writeToObject("output",
                        game.heroes[action.getPlayerIdx() - 1].getMana());
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_CARD_AT_POSITION("getCardAtPosition", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("x", action.getX());
                IOHandler.INSTANCE.writeToObject("y", action.getY());
                Minion minion = game.playedCards[action.getX()][action.getY()];
                if (minion == null) {
                    IOHandler.INSTANCE.writeToObject("output",
                            "No card available at that position.");
                } else {
                    IOHandler.INSTANCE.writeJsonNodeToObject("output",
                            IOHandler.INSTANCE.createNodeFromObject(minion));
                }
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_CARDS_ON_TABLE("getCardsOnTable", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                ArrayList<ArrayList<Minion>> table = new ArrayList<>(TABLE_HEIGHT);
                for (Minion[] line : game.playedCards) {
                    table.add(new ArrayList<>(Arrays.asList(line)));
                    table.getLast().removeIf(Objects::isNull);
                }
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(table));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_CARDS_IN_HAND("getCardsInHand", CommandType.DEBUG) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(game
                                .heroes[action.getPlayerIdx() - 1].getInHandCards()));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_TOTAL_GAMES_PLAYER("getTotalGamesPlayed", CommandType.STATS) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("output",
                        game.currentSession.getPlayerWins(0)
                                + game.currentSession.getPlayerWins(1));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_PLAYER_ONE_WINS("getPlayerOneWins", CommandType.STATS) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("output",
                        game.currentSession.getPlayerWins(0));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_PLAYER_TWO_WINS("getPlayerTwoWins", CommandType.STATS) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("output",
                        game.currentSession.getPlayerWins(1));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        };

        private final String command;
        private final CommandType type;

        Command(final String command, final CommandType type) {
            this.command = command;
            this.type = type;
        }
    }

    private static final HashMap<String, Command> commandsMap = new HashMap<>();
    private final static HashMap<String,
            Function<Quintet<MinionCard, Integer, Integer, Game, Integer>, Minion>> minionConstructor =
            new HashMap<>();

    static {
        minionConstructor.put("Disciple",
                (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
           return new SpecialMinion((SpecialMinionCard) params.getObj0(), params.getObj1(),
                   params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("The Ripper", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new SpecialMinion((SpecialMinionCard) params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("Miraj", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new SpecialMinion((SpecialMinionCard) params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("The Cursed One", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new SpecialMinion((SpecialMinionCard) params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });

        minionConstructor.put("Goliath", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new Minion(params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("Warden", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new Minion(params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("Sentinel", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new Minion(params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });
        minionConstructor.put("Berserker", (Quintet<MinionCard, Integer, Integer, Game, Integer> params) -> {
            return new Minion(params.getObj0(), params.getObj1(),
                    params.getObj2(), params.getObj3(), params.getObj4());
        });

        for (Command value : Command.values()) {
            commandsMap.put(value.command, value);
        }
    }

    //public static ArrayList<MinionCard> cloneDeck(final ArrayList<MinionCard> cardList) {
    //    ArrayList<MinionCard> clonedList = new ArrayList<>(cardList.size());
    //    for (MinionCard card : cardList) {
    //        clonedList.add(new MinionCard(card));
    //    }
    //    return clonedList;
    //}

    public static ArrayList<MinionCard> shuffleDeck(final ArrayList<MinionCard> cardList,
                                                    final int seed) {
        Collections.shuffle(cardList, new Random(seed));
        return cardList;
    }

    public Game(final Session currentSession,
                final int seed,
                final int startingPlayer,
                final ArrayList<MinionCard> player1Deck,
                final ArrayList<MinionCard> player2Deck,
                final Card player1HeroCard,
                final Card player2HeroCard,
                final ArrayList<ActionsInput> actions) {
        this.currentSession = currentSession;
        this.startingPlayer = startingPlayer;
        heroes[0] = new Hero(player1HeroCard,
                shuffleDeck(new ArrayList<>(player1Deck), seed), this, 0);
        heroes[1] = new Hero(player2HeroCard,
                shuffleDeck(new ArrayList<>(player2Deck), seed), this, 1);
        this.actions = actions;
    }

    public void playGame() throws IllegalAccessException {
        for (int i = 0; i < 2; i++) {
            heroes[i].reset(1);
        }

        for (ActionsInput actionInput : actions) {
            Command command = commandsMap.get(actionInput.getCommand());
            if (command == null || (isGameOver && command.getType() == CommandType.GAMEPLAY)) {
                continue;
            }
            IOHandler.INSTANCE.beginObject();
            command.execute(this, actionInput);
            IOHandler.INSTANCE.endObject();
        }
    }

    public int getCurrentTurn() {
        return totalTurns / 2 + 1;
    }

    public int getCurrentPlayerIdx() {
        return totalTurns % 2 == 0 ? startingPlayer : (startingPlayer == 0 ? 1 : 0);
    }

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

    public void onHeroDeath() {
        IOHandler.INSTANCE.beginObject();
        IOHandler.INSTANCE.writeToObject("gameEnded",
                "Player " + (getCurrentPlayerIdx() == 0 ? "one" : "two")
                        + " killed the enemy hero.");
        IOHandler.INSTANCE.writeObjectToOutput();
        IOHandler.INSTANCE.endObject();
        currentSession.addPlayerWin(getCurrentPlayerIdx());
        isGameOver = true;
    }

    public void onEntityDeath(Entity entity) {
        if (entity.getCard().getType() == CardType.HERO) {
            onHeroDeath();
            return;
        }
        onMinionDeath((Minion) entity);
    }

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

    //private static boolean canMinionAct(Minion minion) {
    //    if (minion.isFrozen()) {
    //        IOHandler.INSTANCE.writeToObject("error",
    //                "Attacker card is frozen.");
    //        IOHandler.INSTANCE.writeObjectToOutput();
    //        return true;
    //    }
    //
    //    if (!minion.isCanAct()) {
    //        IOHandler.INSTANCE.writeToObject("error",
    //                "Attacker card has already attacked this turn.");
    //        IOHandler.INSTANCE.writeObjectToOutput();
    //        return true;
    //    }
    //    return false;
    //}
}
