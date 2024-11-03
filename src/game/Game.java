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
import fileio.IOHandler;
import lombok.Getter;

import java.util.ArrayList;

public class Game  {
    public final Session currentSession;
    public final int seed;
    public final int startingPlayer;
    private final Hero[] heroes = new Hero[2];
    private final ArrayList<ActionsInput> actions;

    public static final int tableWidth = 5;
    public static final int tableHeight = 4;
    private final Minion[][] playedCards = new Minion[tableHeight][tableWidth];
    @Getter
    private int totalTurns = 0;
    private boolean isGameOver = false;

    public Game(Session currentSession, int seed, int startingPlayer, ArrayList<MinionCard> player1Deck,
                ArrayList<MinionCard> player2Deck, Card player1HeroCard, Card player2HeroCard,
                ArrayList<ActionsInput> actions) {
        this.currentSession = currentSession;
        this.seed = seed;
        this.startingPlayer = startingPlayer;
        heroes[0] = new Hero(player1HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player1Deck), seed), this, 0);
        heroes[1] = new Hero(player2HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player2Deck), seed), this, 1);
        this.actions = actions;
    }

    public int getCurrentTurn() {
        return totalTurns / 2 + 1;
    }

    public int getCurrentPlayerIdx() {
        return totalTurns % 2 == 0 ? startingPlayer : (startingPlayer == 0 ? 1 : 0);
    }

    private void debugTable() {
        for (int i = 0; i < tableHeight; i++) {
            for (int j = 0; j < tableWidth; j++) {
                System.out.print(playedCards[i][j] == null ? "************\t" :
                        ((playedCards[i][j].isCanAct() ? "" : "-") + (playedCards[i][j].isFrozen() ? "[" : "") + (playedCards[i][j].card.name.toCharArray()[0] == 'T' ? playedCards[i][j].card.name.toCharArray()[4] :
                                playedCards[i][j].card.name.toCharArray()[0]) + (playedCards[i][j].isFrozen() ? "]" : "") + "("
                        + playedCards[i][j].getHealth() + "," + playedCards[i][j].getAttackDamage() + ")\t\t\t"));
                if (j != tableWidth - 1)
                    System.out.print("|");
            }
            System.out.println();
            if (i != tableHeight - 1)
                System.out.println("------------------------------------------------------------------------");
        }
        System.out.println();
    }

    public void playGame() throws IllegalAccessException {
        for (int i = 0; i < 2; i++) {
            heroes[i].addMana(getCurrentTurn());
            heroes[i].drawCard();
        }

        for (ActionsInput action : actions) {
            //System.out.println("Current turn: " + getCurrentTurn() + "  ---  " + "Current player: " + getCurrentPlayerIdx());
            //System.out.println(action);
            switch(action.getCommand()) {
                case "placeCard": {
                    if (isGameOver)
                        break;
                    MinionCard cardToPlace = heroes[getCurrentPlayerIdx()].getCard(action.getHandIdx());
                    if (cardToPlace == null)
                        break;

                    if (cardToPlace.mana > heroes[getCurrentPlayerIdx()].getMana()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "placeCard");
                        IOHandler.getInstance().writeToObject("error", "Not enough mana to place card on table.");
                        IOHandler.getInstance().writeToObject("handIdx", action.getHandIdx());
                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    int x = (cardToPlace.type == CardType.GENERIC || cardToPlace.type == CardType.SPECIAL_BACKLINE) ?
                            (getCurrentPlayerIdx() == 0 ? 3 : 0) : (getCurrentPlayerIdx() == 0 ? 2 : 1);
                    int y = 0;

                    loop:
                    for (int i = 0; i < tableHeight; i++) {
                        if (playedCards[x][i] == null)
                            break loop;
                        y++;
                    }

                    if (y > 4) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "placeCard");
                        IOHandler.getInstance().writeToObject("error", "Cannot place card on table since row is full.");
                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    //System.out.println(cardToPlace);
                    Minion minion = new Minion(cardToPlace, x, y, this, getCurrentPlayerIdx());
                    heroes[getCurrentPlayerIdx()].addMana(-cardToPlace.mana);
                    playedCards[x][y] = minion;
                    heroes[getCurrentPlayerIdx()].removeCard(action.getHandIdx());
                    break;
                }
                case "cardUsesAttack": {
                    if (isGameOver)
                        break;
                    Minion attacker = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    Minion attacked = playedCards[action.getCardAttacked().getX()][action.getCardAttacked().getY()];
                    if (attacker == null || attacked == null)
                        break;
                    if (attacker.ownerPlayerIdx == attacked.ownerPlayerIdx) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAttack");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacked card does not belong to the enemy.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (!attacker.isCanAct()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAttack");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card has already attacked this turn.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (attacker.isFrozen()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAttack");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card is frozen.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    boolean tauntInLine = false;
                    for (var minion : playedCards[getCurrentPlayerIdx() + 1]) {
                        if (minion == null)
                            break;
                        if (minion.card.type == CardType.TAUNT)
                            tauntInLine = true;
                    }

                    if (tauntInLine && attacked.card.type != CardType.TAUNT) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAttack");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacked card is not of type 'Tank'.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    attacker.dealDamage(attacked);
                    attacker.setCanAct(false);
                    break;
                }
                case "cardUsesAbility": {
                    if (isGameOver)
                        break;
                    Minion caster = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    Minion target = playedCards[action.getCardAttacked().getX()][action.getCardAttacked().getY()];
                    if (caster == null || target == null)
                        break;

                    if (caster.isFrozen()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAbility");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card is frozen.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (!caster.isCanAct()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "cardUsesAbility");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacked");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card has already attacked this turn.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (((MinionCard)caster.card).ability == MinionAbility.GODSPLAN) {
                        if (caster.ownerPlayerIdx != target.ownerPlayerIdx) {
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("command", "cardUsesAbility");

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacker");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacked");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().writeToObject("error", "Attacked card does not belong to the current player.");

                            IOHandler.getInstance().writeObjectToOutput();
                            IOHandler.getInstance().endObject();
                            break;
                        }
                    } else {
                        if (caster.ownerPlayerIdx == target.ownerPlayerIdx) {
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("command", "cardUsesAbility");

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacker");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacked");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().writeToObject("error", "Attacked card does not belong to the enemy.");

                            IOHandler.getInstance().writeObjectToOutput();
                            IOHandler.getInstance().endObject();
                            break;
                        }

                        boolean tauntInLine = false;
                        loop:
                        for (var minion : playedCards[getCurrentPlayerIdx() + 1]) {
                            if (minion == null)
                                break loop;
                            if (minion.card.type == CardType.TAUNT) {
                                tauntInLine = true;
                                break loop;
                            }
                        }

                        if (tauntInLine && target.card.type != CardType.TAUNT) {
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("command", "cardUsesAbility");

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacker");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("x", action.getCardAttacked().getX());
                            IOHandler.getInstance().writeToObject("y", action.getCardAttacked().getY());
                            IOHandler.getInstance().writeObjectToObject("cardAttacked");
                            IOHandler.getInstance().endObject();

                            IOHandler.getInstance().writeToObject("error", "Attacked card is not of type 'Tank'.");
                            System.out.println("PlayerIdx = " + getCurrentPlayerIdx() + " " + playedCards[getCurrentPlayerIdx() + 1][0].card);

                            IOHandler.getInstance().writeObjectToOutput();
                            IOHandler.getInstance().endObject();
                            break;
                        }
                    }

                    ((MinionCard)caster.card).ability.useAbility(caster, target);
                    caster.setCanAct(false);
                    break;
                }
                case "useAttackHero": {
                    if (isGameOver)
                        break;
                    Minion attacker = playedCards[action.getCardAttacker().getX()][action.getCardAttacker().getY()];
                    if (attacker == null)
                        break;

                    if (attacker.isFrozen()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "useAttackHero");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card is frozen.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (!attacker.isCanAct()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "useAttackHero");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacker card has already attacked this turn.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    boolean tauntInLine = false;
                    for (var minion : playedCards[getCurrentPlayerIdx() + 1]) {
                        if (minion == null)
                            break;
                        if (minion.card.type == CardType.TAUNT)
                            tauntInLine = true;
                    }

                    if (tauntInLine) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "useAttackHero");

                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("x", action.getCardAttacker().getX());
                        IOHandler.getInstance().writeToObject("y", action.getCardAttacker().getY());
                        IOHandler.getInstance().writeObjectToObject("cardAttacker");
                        IOHandler.getInstance().endObject();

                        IOHandler.getInstance().writeToObject("error", "Attacked card is not of type 'Tank'.");

                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    attacker.dealDamage(heroes[getCurrentPlayerIdx() == 0 ? 1 : 0]);
                    attacker.setCanAct(false);
                    break;
                }
                case "useHeroAbility": {
                    if (heroes[getCurrentPlayerIdx()].card.mana > heroes[getCurrentPlayerIdx()].getMana()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                        IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());
                        IOHandler.getInstance().writeToObject("error", "Not enough mana to use hero's ability.");
                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (!heroes[getCurrentPlayerIdx()].isCanAct()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                        IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());
                        IOHandler.getInstance().writeToObject("error", "Hero has already attacked this turn.");
                        IOHandler.getInstance().writeObjectToOutput();
                        IOHandler.getInstance().endObject();
                        break;
                    }

                    if (((HeroCard)heroes[getCurrentPlayerIdx()].card).ability == HeroAbility.LOWBLOW ||
                            ((HeroCard)heroes[getCurrentPlayerIdx()].card).ability == HeroAbility.SUBZERO) {
                        if ((getCurrentPlayerIdx() == 1 && (action.getAffectedRow() == 0 || action.getAffectedRow() == 1))
                            || (getCurrentPlayerIdx() == 0 && (action.getAffectedRow() == 2 || action.getAffectedRow() == 3))) {
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                            IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());
                            IOHandler.getInstance().writeToObject("error", "Selected row does not belong to the enemy.");
                            IOHandler.getInstance().writeObjectToOutput();
                            IOHandler.getInstance().endObject();
                            break;
                        }
                    }

                    if (((HeroCard)heroes[getCurrentPlayerIdx()].card).ability == HeroAbility.EARTHBORN ||
                            ((HeroCard)heroes[getCurrentPlayerIdx()].card).ability == HeroAbility.BLOODTHIRST) {
                        if ((getCurrentPlayerIdx() == 1 && (action.getAffectedRow() == 2 || action.getAffectedRow() == 3))
                                || (getCurrentPlayerIdx() == 0 && (action.getAffectedRow() == 0 || action.getAffectedRow() == 1))) {
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("command", "useHeroAbility");
                            IOHandler.getInstance().writeToObject("affectedRow", action.getAffectedRow());
                            IOHandler.getInstance().writeToObject("error", "Selected row does not belong to the current player.");
                            IOHandler.getInstance().writeObjectToOutput();
                            IOHandler.getInstance().endObject();
                            break;
                        }
                    }

                    heroes[getCurrentPlayerIdx()].addMana(-heroes[getCurrentPlayerIdx()].card.mana);
                    ((HeroCard)heroes[getCurrentPlayerIdx()].card).ability.useAbility(playedCards[action.getAffectedRow()]);
                    heroes[getCurrentPlayerIdx()].setCanAct(false);
                    System.out.println(heroes[getCurrentPlayerIdx()].card.mana);
                    break;
                }
                case "endPlayerTurn": {
                    if (isGameOver)
                        break;

                    heroes[getCurrentPlayerIdx()].setCanAct(true);
                    for (int i = getCurrentPlayerIdx() == 0 ? 2 : 0; i <= (getCurrentPlayerIdx() == 0 ? 3 : 1); i++)
                        inner_loop:
                        for (Minion minion : playedCards[i]) {
                            if (minion == null)
                                break inner_loop;
                            // Ar trebui sa extrag intr-o functie override-able in Entity pentru reset
                            minion.setFrozen(false);
                            minion.setCanAct(true);
                        }

                    if (getCurrentPlayerIdx() != startingPlayer) {
                        for (int i = 0; i < 2; i++) {
                            heroes[i].addMana(Math.min(getCurrentTurn() + 1, 10));
                            heroes[i].drawCard();
                        }
                    }
                    totalTurns++;
                    break;
                }
                case "getPlayerDeck": {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().beginArray();
                    for (MinionCard card : selectedHero.getAvailableCards()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("mana", card.mana);
                        IOHandler.getInstance().writeToObject("attackDamage", card.attackDamage);
                        IOHandler.getInstance().writeToObject("health", card.health);
                        IOHandler.getInstance().writeToObject("description", card.description);
                        IOHandler.getInstance().beginArray();
                        for (String color : card.colors) {
                            IOHandler.getInstance().writeToArray(color);
                        }
                        IOHandler.getInstance().writeArrayToObject("colors");
                        IOHandler.getInstance().endArray();
                        IOHandler.getInstance().writeToObject("name", card.name);
                        IOHandler.getInstance().writeObjectToArray();
                        IOHandler.getInstance().endObject();
                    }
                    IOHandler.getInstance().writeArrayToObject("output");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerHero": {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("mana", selectedHero.card.mana);
                    IOHandler.getInstance().writeToObject("description", selectedHero.card.description);
                    IOHandler.getInstance().beginArray();
                    for (String color : selectedHero.card.colors)
                        IOHandler.getInstance().writeToArray(color);
                    IOHandler.getInstance().writeArrayToObject("colors");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeToObject("name", selectedHero.card.name);
                    IOHandler.getInstance().writeToObject("health", selectedHero.getHealth());
                    IOHandler.getInstance().writeObjectToObject("output");
                    IOHandler.getInstance().endObject();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerTurn": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", getCurrentPlayerIdx() + 1);
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getTotalGamesPlayed": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output",
                            currentSession.getPlayerWins(0) + currentSession.getPlayerWins(1));
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerOneWins": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", currentSession.getPlayerWins(0));
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerTwoWins": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", currentSession.getPlayerWins(1));
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getFrozenCardsOnTable": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().beginArray();
                    for (Minion[] line : playedCards) {
                        for (Minion minion : line) {
                            if (minion == null)
                                continue;
                            if (minion.isFrozen()) {
                                IOHandler.getInstance().beginObject();
                                IOHandler.getInstance().writeToObject("mana", minion.card.mana);
                                IOHandler.getInstance().writeToObject("attackDamage", minion.getAttackDamage());
                                IOHandler.getInstance().writeToObject("health", minion.getHealth());
                                IOHandler.getInstance().writeToObject("description", minion.card.description);
                                IOHandler.getInstance().beginArray();
                                for (String color : minion.card.colors)
                                    IOHandler.getInstance().writeToArray(color);
                                IOHandler.getInstance().writeArrayToObject("colors");
                                IOHandler.getInstance().endArray();
                                IOHandler.getInstance().writeToObject("name", minion.card.name);
                                IOHandler.getInstance().writeObjectToArray();
                                IOHandler.getInstance().endObject();
                            }
                        }
                    }
                    IOHandler.getInstance().writeArrayToObject("output");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerMana": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().writeToObject("output", heroes[action.getPlayerIdx() - 1].getMana());
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getCardAtPosition": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("x", action.getX());
                    IOHandler.getInstance().writeToObject("y", action.getY());
                    Minion minion = playedCards[action.getX()][action.getY()];
                    if (minion == null) {
                        IOHandler.getInstance().writeToObject("output", "No card available at that position.");
                    } else {
                        IOHandler.getInstance().writeObjectNodeToObject("output", IOHandler.getInstance().createObjectNodeFromObject(minion));
                    }
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getCardsOnTable": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().beginArray();
                    for (Minion[] line : playedCards) {
                        IOHandler.getInstance().beginArray();
                        for (Minion minion : line) {
                            if (minion == null)
                                continue;
                            IOHandler.getInstance().beginObject();
                            IOHandler.getInstance().writeToObject("mana", minion.card.mana);
                            IOHandler.getInstance().writeToObject("attackDamage", minion.getAttackDamage());
                            IOHandler.getInstance().writeToObject("health", minion.getHealth());
                            IOHandler.getInstance().writeToObject("description", minion.card.description);
                            IOHandler.getInstance().beginArray();
                            for (String color : minion.card.colors)
                                IOHandler.getInstance().writeToArray(color);
                            IOHandler.getInstance().writeArrayToObject("colors");
                            IOHandler.getInstance().endArray();
                            IOHandler.getInstance().writeToObject("name", minion.card.name);
                            IOHandler.getInstance().writeObjectToArray();
                            IOHandler.getInstance().endObject();
                        }
                        IOHandler.getInstance().writeArrayToArray();
                        IOHandler.getInstance().endArray();
                    }
                    IOHandler.getInstance().writeArrayToObject("output");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getCardsInHand": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().beginArray();
                    for (MinionCard minion : heroes[action.getPlayerIdx() - 1].getInHandCards()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("mana", minion.mana);
                        IOHandler.getInstance().writeToObject("attackDamage", minion.attackDamage);
                        IOHandler.getInstance().writeToObject("health", minion.health);
                        IOHandler.getInstance().writeToObject("description", minion.description);
                        IOHandler.getInstance().beginArray();
                        for (String color : minion.colors)
                            IOHandler.getInstance().writeToArray(color);
                        IOHandler.getInstance().writeArrayToObject("colors");
                        IOHandler.getInstance().endArray();
                        IOHandler.getInstance().writeToObject("name", minion.name);
                        IOHandler.getInstance().writeObjectToArray();
                        IOHandler.getInstance().endObject();
                    }
                    IOHandler.getInstance().writeArrayToObject("output");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                default:
                    break;
            }

            //if (!action.getCommand().equals("getCardAtPosition") &&
            //        !action.getCommand().equals("getPlayerDeck") &&
            //        !action.getCommand().equals("cardUsesAttack") &&
            //        !action.getCommand().equals("cardUsesAbility") &&
            //        !action.getCommand().equals("useAttackHero") &&
            //        !action.getCommand().equals("getPlayerTurn") &&
            //        !action.getCommand().equals("getCardsOnTable") &&
            //        !action.getCommand().equals("getCardsInHand"))
            //    System.out.println("P0: " + heroes[0].getMana() + "\t\t\t" + "P1: " +heroes[1].getMana());

            //if (!action.getCommand().equals("getCardAtPosition") &&
            //        !action.getCommand().equals("endPlayerTurn") &&
            //        !action.getCommand().equals("getPlayerMana") &&
            //        !action.getCommand().equals("getPlayerDeck") &&
            //        !action.getCommand().equals("getPlayerTurn") &&
            //        !action.getCommand().equals("getCardsOnTable") &&
            //        !action.getCommand().equals("getCardsInHand"))
            //    debugTable();
            //System.out.println();
            //System.out.println();
        }
    }

    public void onMinionDeath(Minion minion) {
        for (int i = minion.getY(); i < tableWidth - 1; i++) {
            playedCards[minion.getX()][i] = playedCards[minion.getX()][i + 1];
            if (playedCards[minion.getX()][i] == null)
                break;
            playedCards[minion.getX()][i].setY(i);
        }
        playedCards[minion.getX()][tableWidth - 1] = null;
    }

    public void onHeroDeath(Hero hero) {
        IOHandler.getInstance().beginObject();
        IOHandler.getInstance().writeToObject("gameEnded",
                "Player " + (getCurrentPlayerIdx() == 0 ? "one" : "two") + " killed the enemy hero.");
        IOHandler.getInstance().writeObjectToOutput();
        IOHandler.getInstance().endObject();
        currentSession.addPlayerWin(getCurrentPlayerIdx());
        isGameOver = true;
    }

    public void onEntityDeath(Entity entity) {
        if (entity.card.type == CardType.HERO) {
            onHeroDeath((Hero) entity);
            return;
        }
        onMinionDeath((Minion) entity);
    }
}
