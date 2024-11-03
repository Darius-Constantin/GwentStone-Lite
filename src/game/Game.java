package game;

import cards.Card;
import cards.CardType;
import cards.HeroAbility;
import cards.HeroCard;
import cards.MinionAbility;
import cards.MinionCard;
import entities.Hero;
import entities.Entity;
import entities.Minion;
import fileio.ActionsInput;
import fileio.Command;
import fileio.CommandType;
import fileio.IOHandler;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Game  {
    private final Session currentSession;
    private final int startingPlayer;
    private final Hero[] heroes = new Hero[2];
    private final ArrayList<ActionsInput> actions;

    public static final int TABLEHEIGHT = 4;
    public static final int TABLEWIDTH = 5;
    public static final int P1BACK = 3;
    public static final int P1FRONT = 2;
    public static final int P2FRONT = 1;
    public static final int P2BACK = 0;
    public static final int MAXMANAGAIN = 10;

    private final Minion[][] playedCards = new Minion[TABLEHEIGHT][TABLEWIDTH];
    @Getter
    private int totalTurns = 0;
    private boolean isGameOver = false;

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
        heroes[0] = new Hero(player1HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player1Deck), seed),
                this, 0);
        heroes[1] = new Hero(player2HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player2Deck), seed),
                this, 1);
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

        for (ActionsInput action : actions) {
            final int currentPlayerIdx = getCurrentPlayerIdx();
            Command command = Command.getCommandFromString(action.getCommand());
            if (isGameOver && command.getCommandType() == CommandType.GAMEPLAY) {
                continue;
            }
            IOHandler.getInstance().beginObject();
            switch (command) {
                case PLACECARD: {
                    MinionCard cardToPlace = heroes[currentPlayerIdx].getCard(action.getHandIdx());
                    if (cardToPlace == null) {
                        break;
                    }

                    IOHandler.getInstance().writeToObject("command", "placeCard");

                    if (cardToPlace.getMana() > heroes[currentPlayerIdx].getMana()) {
                        IOHandler.getInstance().writeToObject("command", "placeCard");
                        IOHandler.getInstance().writeToObject("error",
                                "Not enough mana to place card on table.");
                        IOHandler.getInstance().writeToObject("handIdx", action.getHandIdx());
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    int x = (cardToPlace.getType() == CardType.GENERIC
                            || cardToPlace.getType() == CardType.SPECIAL_BACKLINE) ?
                            (currentPlayerIdx == 0 ? P1BACK : P2BACK) : (currentPlayerIdx == 0 ? P1FRONT : P2FRONT);
                    int y = 0;

                    loop:
                    for (int i = 0; i < TABLEHEIGHT; i++) {
                        if (playedCards[x][i] == null) {
                            break loop;
                        }
                        y++;
                    }

                    if (y > TABLEWIDTH - 1) {
                        IOHandler.getInstance().writeToObject("error",
                                "Cannot place card on table since row is full.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    Minion minion = new Minion(cardToPlace, x, y, this, currentPlayerIdx);
                    heroes[currentPlayerIdx].addMana(-cardToPlace.getMana());
                    playedCards[x][y] = minion;
                    heroes[currentPlayerIdx].removeCard(action.getHandIdx());
                    break;
                }
                case CARDUSESATTACK: {
                    Minion attacker = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    Minion attacked = playedCards[action.getCardAttacked().getX()][action.getCardAttacked().getY()];

                    IOHandler.getInstance().writeToObject("command", "cardUsesAttack");
                    IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                            IOHandler.getInstance().createObjectNodeFromObject(action.getCardAttacker()));
                    IOHandler.getInstance().writeJsonNodeToObject("cardAttacked",
                            IOHandler.getInstance().createObjectNodeFromObject(action.getCardAttacked()));

                    if (attacker == null || attacked == null) {
                        break;
                    }
                    if (attacker.ownerPlayerIdx == attacked.ownerPlayerIdx) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacked card does not belong to the enemy.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (!attacker.isCanAct()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacker card has already attacked this turn.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (attacker.isFrozen()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacker card is frozen.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    boolean tauntInLine = false;
                    for (var minion : playedCards[currentPlayerIdx + 1]) {
                        if (minion == null) {
                            break;
                        }
                        if (minion.card.getType() == CardType.TAUNT) {
                            tauntInLine = true;
                        }
                    }

                    if (tauntInLine && attacked.card.getType() != CardType.TAUNT) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacked card is not of type 'Tank'.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    attacker.dealDamage(attacked);
                    attacker.setCanAct(false);
                    break;
                }
                case CARDUSESABILITY: {
                    Minion caster = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    Minion target = playedCards[action.getCardAttacked().getX()][action.getCardAttacked().getY()];
                    if (caster == null || target == null) {
                        break;
                    }

                    IOHandler.getInstance().writeToObject("command", "cardUsesAbility");
                    IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                            IOHandler.getInstance().createObjectNodeFromObject(action.getCardAttacker()));
                    IOHandler.getInstance().writeJsonNodeToObject("cardAttacked",
                            IOHandler.getInstance().createObjectNodeFromObject(action.getCardAttacked()));


                    if (caster.isFrozen()) {
                        IOHandler.getInstance().writeToObject("error", "Attacker card is frozen.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (!caster.isCanAct()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacker card has already attacked this turn.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (((MinionCard) caster.card).ability == MinionAbility.GODSPLAN) {
                        if (caster.ownerPlayerIdx != target.ownerPlayerIdx) {
                            IOHandler.getInstance().writeToObject("error",
                                    "Attacked card does not belong to the current player.");
                            IOHandler.getInstance().writeObjectToOutput();
                            break;
                        }
                    } else {
                        if (caster.ownerPlayerIdx == target.ownerPlayerIdx) {
                            IOHandler.getInstance().writeToObject("error",
                                    "Attacked card does not belong to the enemy.");
                            IOHandler.getInstance().writeObjectToOutput();
                            break;
                        }

                        boolean tauntInLine = false;
                        loop:
                        for (var minion : playedCards[currentPlayerIdx + 1]) {
                            if (minion == null) {
                                break loop;
                            }
                            if (minion.card.getType() == CardType.TAUNT) {
                                tauntInLine = true;
                                break loop;
                            }
                        }

                        if (tauntInLine && target.card.getType() != CardType.TAUNT) {
                            IOHandler.getInstance().writeToObject("error",
                                    "Attacked card is not of type 'Tank'.");
                            IOHandler.getInstance().writeObjectToOutput();
                            break;
                        }
                    }

                    ((MinionCard) caster.card).ability.useAbility(caster, target);
                    caster.setCanAct(false);
                    break;
                }
                case USEATTACKHERO: {
                    Minion attacker = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    if (attacker == null) {
                        break;
                    }

                    IOHandler.getInstance().writeToObject("command", "useAttackHero");
                    IOHandler.getInstance().writeJsonNodeToObject("cardAttacker",
                            IOHandler.getInstance().createObjectNodeFromObject(action.getCardAttacker()));

                    if (attacker.isFrozen()) {
                        IOHandler.getInstance().writeToObject("error", "Attacker card is frozen.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (!attacker.isCanAct()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacker card has already attacked this turn.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    boolean tauntInLine = false;
                    for (var minion : playedCards[currentPlayerIdx + 1]) {
                        if (minion == null) {
                            break;
                        }
                        if (minion.card.getType() == CardType.TAUNT) {
                            tauntInLine = true;
                        }
                    }

                    if (tauntInLine) {
                        IOHandler.getInstance().writeToObject("error",
                                "Attacked card is not of type 'Tank'.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    attacker.dealDamage(heroes[currentPlayerIdx == 0 ? 1 : 0]);
                    attacker.setCanAct(false);
                    break;
                }
                case USEHEROABILITY: {
                    IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                    IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());
                    if (heroes[currentPlayerIdx].card.getMana() > heroes[currentPlayerIdx].getMana()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Not enough mana to use hero's ability.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    if (!heroes[currentPlayerIdx].isCanAct()) {
                        IOHandler.getInstance().writeToObject("error",
                                "Hero has already attacked this turn.");
                        IOHandler.getInstance().writeObjectToOutput();
                        break;
                    }

                    HeroCard currentHero = (HeroCard) heroes[currentPlayerIdx].card;
                    if (currentHero.getAbility() == HeroAbility.LOWBLOW
                            || currentHero.getAbility()  == HeroAbility.SUBZERO) {
                        if ((currentPlayerIdx == 1
                                && (action.getAffectedRow() == P2BACK || action.getAffectedRow() == P2FRONT))
                                || (currentPlayerIdx == 0
                                && (action.getAffectedRow() == P1FRONT || action.getAffectedRow() == P1BACK))) {
                            IOHandler.getInstance().writeToObject("error",
                                    "Selected row does not belong to the enemy.");
                            IOHandler.getInstance().writeObjectToOutput();
                            break;
                        }
                    }

                    if (currentHero.getAbility() == HeroAbility.EARTHBORN ||
                            currentHero.getAbility() == HeroAbility.BLOODTHIRST) {
                        if ((currentPlayerIdx == 1
                                && (action.getAffectedRow() == P1FRONT || action.getAffectedRow() == P1BACK))
                                || (currentPlayerIdx == 0
                                && (action.getAffectedRow() == P2BACK || action.getAffectedRow() == P2FRONT))) {
                            IOHandler.getInstance().writeToObject("error",
                                    "Selected row does not belong to the current player.");
                            IOHandler.getInstance().writeObjectToOutput();
                            break;
                        }
                    }

                    heroes[currentPlayerIdx].addMana(-heroes[currentPlayerIdx].card.getMana());
                    ((HeroCard) heroes[currentPlayerIdx].card).getAbility()
                            .useAbility(playedCards[action.getAffectedRow()]);
                    heroes[currentPlayerIdx].setCanAct(false);
                    break;
                }
                case ENDPLAYERTURN: {
                    heroes[currentPlayerIdx].setCanAct(true);
                    for (int i = currentPlayerIdx == 0 ? P1FRONT : P2BACK;
                         i <= (currentPlayerIdx == 0 ? P1BACK : P2FRONT); i++)
                        inner_loop:
                        for (Minion minion : playedCards[i]) {
                            if (minion == null) {
                                break inner_loop;
                            }
                            minion.reset();
                        }

                    if (currentPlayerIdx != startingPlayer) {
                        for (int i = 0; i < 2; i++) {
                            heroes[i].reset(Math.min(getCurrentTurn() + 1, MAXMANAGAIN));
                        }
                    }
                    totalTurns++;
                    break;
                }
                case GETPLAYERDECK: {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance()
                                    .createArrayNodeFromArrayOfObjects(selectedHero.getAvailableCards()));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETPLAYERHERO: {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance().createObjectNodeFromObject(selectedHero));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETPLAYERTURN: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", currentPlayerIdx + 1);
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETFROZENCARDSONTABLE: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    ArrayList<Minion> frozenMinions = new ArrayList<>(TABLEHEIGHT * TABLEWIDTH);
                    for (Minion[] line : playedCards) {
                        frozenMinions.addAll(new ArrayList<>(Arrays.asList(line)));
                    }
                    frozenMinions.removeIf(minion -> (minion == null || !minion.isFrozen()));
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance().createArrayNodeFromArrayOfObjects(frozenMinions));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETPLAYERMANA: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().writeToObject("output", heroes[action.getPlayerIdx() - 1].getMana());
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETCARDATPOSITION: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("x", action.getX());
                    IOHandler.getInstance().writeToObject("y", action.getY());
                    Minion minion = playedCards[action.getX()][action.getY()];
                    if (minion == null) {
                        IOHandler.getInstance().writeToObject("output",
                                "No card available at that position.");
                    } else {
                        IOHandler.getInstance().writeJsonNodeToObject("output",
                                IOHandler.getInstance().createObjectNodeFromObject(minion));
                    }
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETCARDSONTABLE: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    ArrayList<ArrayList<Minion>> table = new ArrayList<>(TABLEHEIGHT);
                    for (Minion[] line : playedCards) {
                        table.add(new ArrayList<>(Arrays.asList(line)));
                        table.get(table.size() - 1).removeIf(Objects::isNull);
                    }
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance().createArrayNodeFromArrayOfObjects(table));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETCARDSINHAND: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().writeJsonNodeToObject("output",
                            IOHandler.getInstance().createArrayNodeFromArrayOfObjects(heroes[action.getPlayerIdx() - 1].getInHandCards()));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETTOTALGAMESPLAYED: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output",
                            currentSession.getPlayerWins(0) + currentSession.getPlayerWins(1));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETPLAYERONEWINS: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", currentSession.getPlayerWins(0));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                case GETPLAYERTWOWINS: {
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", currentSession.getPlayerWins(1));
                    IOHandler.getInstance().writeObjectToOutput();
                    break;
                }
                default:
                    break;
            }

            IOHandler.getInstance().endObject();
        }
    }

    public void onMinionDeath(final Minion minion) {
        for (int i = minion.getY(); i < TABLEWIDTH - 1; i++) {
            playedCards[minion.getX()][i] = playedCards[minion.getX()][i + 1];
            if (playedCards[minion.getX()][i] == null) {
                break;
            }
            playedCards[minion.getX()][i].setY(i);
        }
        playedCards[minion.getX()][TABLEWIDTH - 1] = null;
    }

    public void onHeroDeath(final Hero hero) {
        IOHandler.getInstance().beginObject();
        IOHandler.getInstance().writeToObject("gameEnded",
                "Player " + (getCurrentPlayerIdx() == 0 ? "one" : "two") + " killed the enemy hero.");
        IOHandler.getInstance().writeObjectToOutput();
        IOHandler.getInstance().endObject();
        currentSession.addPlayerWin(getCurrentPlayerIdx());
        isGameOver = true;
    }

    public void onEntityDeath(Entity entity) {
        if (entity.card.getType() == CardType.HERO) {
            onHeroDeath((Hero) entity);
            return;
        }
        onMinionDeath((Minion) entity);
    }
}
