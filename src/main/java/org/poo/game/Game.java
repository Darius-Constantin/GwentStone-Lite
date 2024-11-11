package org.poo.game;

import org.poo.game.cards.Card;
import org.poo.game.cards.CardType;
import org.poo.game.cards.HeroAbility;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionAbility;
import org.poo.game.cards.MinionCard;
import org.poo.game.entities.Hero;
import org.poo.game.entities.Entity;
import org.poo.game.entities.Minion;
import org.poo.fileio.ActionsInput;
import org.poo.fileio.IOHandler;
import lombok.Getter;
import org.poo.meta_game.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

                Minion minion = new Minion(cardToPlace, x, y, game, currentPlayerIdx);
                game.heroes[currentPlayerIdx].addMana(-cardToPlace.getMana());
                game.playedCards[x][y] = minion;
                game.heroes[currentPlayerIdx].removeCard(action.getHandIdx());
            }
        },
        CARD_USES_ATTACK("cardUsesAttack", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
                var cardAttacker = action.getCardAttacker();
                var cardAttacked = action.getCardAttacked();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                Minion attacked = game.playedCards[cardAttacked.getX()][cardAttacked.getY()];

                IOHandler.INSTANCE.writeToObject("command", "cardUsesAttack");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacked()));

                if (attacker == null || attacked == null) {
                    return;
                }
                if (attacker.getOwnerPlayerIdx() == attacked.getOwnerPlayerIdx()) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Attacked card does not belong to the enemy.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                if (!attacker.isCanAct()) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Attacker card has already attacked this turn.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                if (attacker.isFrozen()) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Attacker card is frozen.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                if (game.isTauntOnEnemySide() && attacked.getCard().getType() != CardType.TAUNT) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Attacked card is not of type 'Tank'.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                attacker.dealDamage(attacked);
                attacker.setCanAct(false);
            }
        },
        CARD_USES_ABILITY("cardUsesAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
                var cardAttacker = action.getCardAttacker();
                var cardAttacked = action.getCardAttacked();
                Minion caster = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                Minion target = game.playedCards[cardAttacked.getX()][cardAttacked.getY()];
                if (caster == null || target == null) {
                    return;
                }

                IOHandler.INSTANCE.writeToObject("command", "cardUsesAbility");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacked()));


                if (canMinionAct(caster)) {
                    return;
                }

                if (((MinionCard) caster.getCard()).getAbility() == MinionAbility.GODSPLAN) {
                    if (caster.getOwnerPlayerIdx() != target.getOwnerPlayerIdx()) {
                        IOHandler.INSTANCE.writeToObject("error",
                                "Attacked card does not belong to the current player.");
                        IOHandler.INSTANCE.writeObjectToOutput();
                        return;
                    }
                } else {
                    if (caster.getOwnerPlayerIdx() == target.getOwnerPlayerIdx()) {
                        IOHandler.INSTANCE.writeToObject("error",
                                "Attacked card does not belong to the enemy.");
                        IOHandler.INSTANCE.writeObjectToOutput();
                        return;
                    }

                    if (game.isTauntOnEnemySide() && target.getCard().getType() != CardType.TAUNT) {
                        IOHandler.INSTANCE.writeToObject("error",
                                "Attacked card is not of type 'Tank'.");
                        IOHandler.INSTANCE.writeObjectToOutput();
                        return;
                    }
                }

                ((MinionCard) caster.getCard()).getAbility().useAbility(caster, target);
                caster.setCanAct(false);
            }
        },
        USE_ATTACK_HERO("useAttackHero", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                var cardAttacker = action.getCardAttacker();
                Minion attacker = game.playedCards[cardAttacker.getX()][cardAttacker.getY()];
                if (attacker == null) {
                    return;
                }

                IOHandler.INSTANCE.writeToObject("command", "useAttackHero");
                IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                        IOHandler.INSTANCE
                                .createNodeFromObject(action.getCardAttacker()));

                if (canMinionAct(attacker)) {
                    return;
                }

                boolean tauntInLine = false;
                for (var minion : game.playedCards[currentPlayerIdx + 1]) {
                    if (minion == null) {
                        break;
                    }
                    if (minion.getCard().getType() == CardType.TAUNT) {
                        tauntInLine = true;
                        break;
                    }
                }

                if (tauntInLine) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Attacked card is not of type 'Tank'.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                attacker.dealDamage(game.heroes[currentPlayerIdx == 0 ? 1 : 0]);
                attacker.setCanAct(false);
            }
        },
        USE_HERO_ABILITY("useHeroAbility", CommandType.GAMEPLAY) {
            @Override
            public void execute(Game game, ActionsInput action) {
                final int currentPlayerIdx = game.getCurrentPlayerIdx();
                IOHandler.INSTANCE.writeToObject("command", "useHeroAbility");
                IOHandler.INSTANCE.writeToObject("affectedRow", action.getAffectedRow());
                if (game.heroes[currentPlayerIdx].getCard().getMana()
                        > game.heroes[currentPlayerIdx].getMana()) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Not enough mana to use hero's ability.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                if (!game.heroes[currentPlayerIdx].isCanAct()) {
                    IOHandler.INSTANCE.writeToObject("error",
                            "Hero has already attacked this turn.");
                    IOHandler.INSTANCE.writeObjectToOutput();
                    return;
                }

                HeroCard currentHero = (HeroCard) game.heroes[currentPlayerIdx].getCard();
                if (currentHero.getAbility() == HeroAbility.LOWBLOW
                        || currentHero.getAbility() == HeroAbility.SUBZERO) {
                    if ((currentPlayerIdx == 1
                            && (action.getAffectedRow() == P2_BACK
                            || action.getAffectedRow() == P2_FRONT))
                            || (currentPlayerIdx == 0
                            && (action.getAffectedRow() == P1_FRONT
                            || action.getAffectedRow() == P1_BACK))) {
                        IOHandler.INSTANCE.writeToObject("error",
                                "Selected row does not belong to the enemy.");
                        IOHandler.INSTANCE.writeObjectToOutput();
                        return;
                    }
                }

                if (currentHero.getAbility() == HeroAbility.EARTHBORN
                        || currentHero.getAbility() == HeroAbility.BLOODTHIRST) {
                    if ((currentPlayerIdx == 1
                            && (action.getAffectedRow() == P1_FRONT
                            || action.getAffectedRow() == P1_BACK))
                            || (currentPlayerIdx == 0
                            && (action.getAffectedRow() == P2_BACK
                            || action.getAffectedRow() == P2_FRONT))) {
                        IOHandler.INSTANCE.writeToObject("error",
                                "Selected row does not belong to the current player.");
                        IOHandler.INSTANCE.writeObjectToOutput();
                        return;
                    }
                }

                game.heroes[currentPlayerIdx].addMana(-game.heroes[currentPlayerIdx].getCard().getMana());
                ((HeroCard) game.heroes[currentPlayerIdx].getCard()).getAbility()
                        .useAbility(game.playedCards[action.getAffectedRow()]);
                game.heroes[currentPlayerIdx].setCanAct(false);
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
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
            public void execute(Game game, ActionsInput action) throws IllegalAccessException {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
                IOHandler.INSTANCE.writeJsonNodeToObject("output",
                        IOHandler.INSTANCE.createNodeFromObject(game.heroes[action.getPlayerIdx() - 1].getInHandCards()));
                IOHandler.INSTANCE.writeObjectToOutput();
            }
        },
        GET_TOTAL_GAMES_PLAYER("getTotalGamesPlayed", CommandType.STATS) {
            @Override
            public void execute(Game game, ActionsInput action) {
                IOHandler.INSTANCE.writeToObject("command", action.getCommand());
                IOHandler.INSTANCE.writeToObject("output",
                        game.currentSession
                                .getPlayerWins(0) + game.currentSession.getPlayerWins(1));
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

        private static boolean canMinionAct(Minion caster) {
            if (caster.isFrozen()) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Attacker card is frozen.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return true;
            }

            if (!caster.isCanAct()) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Attacker card has already attacked this turn.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return true;
            }
            return false;
        }

        private final String command;
        private final CommandType commandType;

        Command(final String command, final CommandType type) {
            this.command = command;
            this.commandType = type;
        }

        public static Command getCommandFromString(final String command) {
            for (var type : Command.values()) {
                if (type.command.equals(command)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Illegal command: " + command);
        }
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
                MinionCard.shuffleDeck(MinionCard.cloneDeck(player1Deck), seed), this, 0);
        heroes[1] = new Hero(player2HeroCard,
                MinionCard.shuffleDeck(MinionCard.cloneDeck(player2Deck), seed), this, 1);
        this.actions = actions;
    }

    public int getCurrentTurn() {
        return totalTurns / 2 + 1;
    }

    public int getCurrentPlayerIdx() {
        return totalTurns % 2 == 0 ? startingPlayer : (startingPlayer == 0 ? 1 : 0);
    }

    public void playGame() throws IllegalAccessException {
        for (int i = 0; i < 2; i++) {
            heroes[i].reset(1);
        }

        for (ActionsInput actionInput : actions) {
            Command command = Command.getCommandFromString(actionInput.getCommand());
            if (isGameOver && command.getCommandType() == CommandType.GAMEPLAY) {
                continue;
            }
            IOHandler.INSTANCE.beginObject();
            command.execute(this, actionInput);
            IOHandler.INSTANCE.endObject();
        }
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

    private boolean isTauntOnEnemySide() {
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
}
