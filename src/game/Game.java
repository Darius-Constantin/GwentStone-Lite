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

        for (ActionsInput action : actions) {
            Command command = Command.getCommandFromString(action.getCommand());
            if (isGameOver && command.getCommandType() == CommandType.GAMEPLAY) {
                continue;
            }
            IOHandler.INSTANCE.beginObject();
            switch (command) {
                case PLACECARD:
                    placeCard(action);
                    break;
                case CARDUSESATTACK:
                    cardUsesAttack(action);
                    break;
                case CARDUSESABILITY:
                    cardUsesAbility(action);
                    break;
                case USEATTACKHERO:
                    useAttackHero(action);
                    break;
                case USEHEROABILITY:
                    useHeroAbility(action);
                    break;
                case ENDPLAYERTURN:
                    endPlayerTurn();
                    break;
                case GETPLAYERDECK:
                    getPlayerDeck(action);
                    break;
                case GETPLAYERHERO:
                    getPlayerHero(action);
                    break;
                case GETPLAYERTURN:
                    getPlayerTurn(action);
                    break;
                case GETFROZENCARDSONTABLE:
                    getFrozenCardsOnTable(action);
                    break;
                case GETPLAYERMANA:
                    getPlayerMana(action);
                    break;
                case GETCARDATPOSITION:
                    getCardAtPosition(action);
                    break;
                case GETCARDSONTABLE:
                    getCardsOnTable(action);
                    break;
                case GETCARDSINHAND:
                    getCardsInHand(action);
                    break;
                case GETTOTALGAMESPLAYED:
                    getTotalGamesPlayed(action);
                    break;
                case GETPLAYERONEWINS:
                    getPlayerOneWins(action);
                    break;
                case GETPLAYERTWOWINS:
                    getPlayerTwoWins(action);
                    break;
                default:
                    break;
            }

            IOHandler.INSTANCE.endObject();
        }
    }

    private void getPlayerTwoWins(ActionsInput action) {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("output",
                currentSession.getPlayerWins(1));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getPlayerOneWins(ActionsInput action) {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("output",
                currentSession.getPlayerWins(0));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getTotalGamesPlayed(ActionsInput action) {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("output",
                currentSession
                        .getPlayerWins(0) + currentSession.getPlayerWins(1));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getCardsInHand(ActionsInput action) throws IllegalAccessException {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
        IOHandler.INSTANCE.writeJsonNodeToObject("output",
                IOHandler.INSTANCE
                        .createArrayNodeFromArray(heroes[action.getPlayerIdx() - 1]
                                .getInHandCards()));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getCardsOnTable(ActionsInput action) throws IllegalAccessException {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        ArrayList<ArrayList<Minion>> table = new ArrayList<>(TABLEHEIGHT);
        for (Minion[] line : playedCards) {
            table.add(new ArrayList<>(Arrays.asList(line)));
            table.get(table.size() - 1).removeIf(Objects::isNull);
        }
        IOHandler.INSTANCE.writeJsonNodeToObject("output",
                IOHandler.INSTANCE.createArrayNodeFromArray(table));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getCardAtPosition(ActionsInput action) throws IllegalAccessException {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("x", action.getX());
        IOHandler.INSTANCE.writeToObject("y", action.getY());
        Minion minion = playedCards[action.getX()][action.getY()];
        if (minion == null) {
            IOHandler.INSTANCE.writeToObject("output",
                    "No card available at that position.");
        } else {
            IOHandler.INSTANCE.writeJsonNodeToObject("output",
                    IOHandler.INSTANCE.createObjectNodeFromObject(minion));
        }
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getPlayerMana(ActionsInput action) {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
        IOHandler.INSTANCE.writeToObject("output",
                heroes[action.getPlayerIdx() - 1].getMana());
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getFrozenCardsOnTable(ActionsInput action) throws IllegalAccessException {
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        ArrayList<Minion> frozenMinions = new ArrayList<>(TABLEHEIGHT * TABLEWIDTH);
        for (Minion[] line : playedCards) {
            frozenMinions.addAll(new ArrayList<>(Arrays.asList(line)));
        }
        frozenMinions.removeIf(minion -> (minion == null || !minion.isFrozen()));
        IOHandler.INSTANCE.writeJsonNodeToObject("output",
                IOHandler.INSTANCE.createArrayNodeFromArray(frozenMinions));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getPlayerTurn(ActionsInput action) {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("output", currentPlayerIdx + 1);
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getPlayerHero(ActionsInput action) throws IllegalAccessException {
        Hero selectedHero = heroes[action.getPlayerIdx() - 1];
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
        IOHandler.INSTANCE.writeJsonNodeToObject("output",
                IOHandler.INSTANCE.createObjectNodeFromObject(selectedHero));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void getPlayerDeck(ActionsInput action) throws IllegalAccessException {
        Hero selectedHero = heroes[action.getPlayerIdx() - 1];
        IOHandler.INSTANCE.writeToObject("command", action.getCommand());
        IOHandler.INSTANCE.writeToObject("playerIdx", action.getPlayerIdx());
        IOHandler.INSTANCE.writeJsonNodeToObject("output",
                IOHandler.INSTANCE
                        .createArrayNodeFromArray(selectedHero
                                .getAvailableCards()));
        IOHandler.INSTANCE.writeObjectToOutput();
    }

    private void endPlayerTurn() {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        heroes[currentPlayerIdx].setCanAct(true);
        for (int i = currentPlayerIdx == 0 ? P1FRONT : P2BACK;
             i <= (currentPlayerIdx == 0 ? P1BACK : P2FRONT); i++) {
            inner_loop:
            for (Minion minion : playedCards[i]) {
                if (minion == null) {
                    break inner_loop;
                }
                minion.reset();
            }
        }

        if (currentPlayerIdx != startingPlayer) {
            for (int i = 0; i < 2; i++) {
                heroes[i].reset(Math.min(getCurrentTurn() + 1, MAXMANAGAIN));
            }
        }
        totalTurns++;
    }

    private void useHeroAbility(ActionsInput action) {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        IOHandler.INSTANCE.writeToObject("command", "useHeroAbility");
        IOHandler.INSTANCE.writeToObject("affectedRow", action.getAffectedRow());
        if (heroes[currentPlayerIdx].getCard().getMana()
                > heroes[currentPlayerIdx].getMana()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Not enough mana to use hero's ability.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (!heroes[currentPlayerIdx].isCanAct()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Hero has already attacked this turn.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        HeroCard currentHero = (HeroCard) heroes[currentPlayerIdx].getCard();
        if (currentHero.getAbility() == HeroAbility.LOWBLOW
                || currentHero.getAbility()  == HeroAbility.SUBZERO) {
            if ((currentPlayerIdx == 1
                    && (action.getAffectedRow() == P2BACK
                    || action.getAffectedRow() == P2FRONT))
                    || (currentPlayerIdx == 0
                    && (action.getAffectedRow() == P1FRONT
                    || action.getAffectedRow() == P1BACK))) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Selected row does not belong to the enemy.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return;
            }
        }

        if (currentHero.getAbility() == HeroAbility.EARTHBORN
                || currentHero.getAbility() == HeroAbility.BLOODTHIRST) {
            if ((currentPlayerIdx == 1
                    && (action.getAffectedRow() == P1FRONT
                    || action.getAffectedRow() == P1BACK))
                    || (currentPlayerIdx == 0
                    && (action.getAffectedRow() == P2BACK
                    || action.getAffectedRow() == P2FRONT))) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Selected row does not belong to the current player.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return;
            }
        }

        heroes[currentPlayerIdx].addMana(-heroes[currentPlayerIdx].getCard().getMana());
        ((HeroCard) heroes[currentPlayerIdx].getCard()).getAbility()
                .useAbility(playedCards[action.getAffectedRow()]);
        heroes[currentPlayerIdx].setCanAct(false);
    }

    private void useAttackHero(ActionsInput action) throws IllegalAccessException {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        var cardAttacker = action.getCardAttacker();
        Minion attacker = playedCards[cardAttacker.getX()][cardAttacker.getY()];
        if (attacker == null) {
            return;
        }

        IOHandler.INSTANCE.writeToObject("command", "useAttackHero");
        IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                IOHandler.INSTANCE
                        .createObjectNodeFromObject(action.getCardAttacker()));

        if (attacker.isFrozen()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card is frozen.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (!attacker.isCanAct()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card has already attacked this turn.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        boolean tauntInLine = false;
        for (var minion : playedCards[currentPlayerIdx + 1]) {
            if (minion == null) {
                break;
            }
            if (minion.getCard().getType() == CardType.TAUNT) {
                tauntInLine = true;
            }
        }

        if (tauntInLine) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card is not of type 'Tank'.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        attacker.dealDamage(heroes[currentPlayerIdx == 0 ? 1 : 0]);
        attacker.setCanAct(false);
    }

    private void cardUsesAbility(ActionsInput action) throws IllegalAccessException {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        var cardAttacker = action.getCardAttacker();
        var cardAttacked = action.getCardAttacked();
        Minion caster = playedCards[cardAttacker.getX()][cardAttacker.getY()];
        Minion target = playedCards[cardAttacked.getX()][cardAttacked.getY()];
        if (caster == null || target == null) {
            return;
        }

        IOHandler.INSTANCE.writeToObject("command", "cardUsesAbility");
        IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                IOHandler.INSTANCE
                        .createObjectNodeFromObject(action.getCardAttacker()));
        IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                IOHandler.INSTANCE
                        .createObjectNodeFromObject(action.getCardAttacked()));


        if (caster.isFrozen()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card is frozen.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (!caster.isCanAct()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card has already attacked this turn.");
            IOHandler.INSTANCE.writeObjectToOutput();
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

            boolean tauntInLine = false;
            loop:
            for (var minion : playedCards[currentPlayerIdx + 1]) {
                if (minion == null) {
                    break loop;
                }
                if (minion.getCard().getType() == CardType.TAUNT) {
                    tauntInLine = true;
                    break loop;
                }
            }

            if (tauntInLine && target.getCard().getType() != CardType.TAUNT) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Attacked card is not of type 'Tank'.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return;
            }
        }

        ((MinionCard) caster.getCard()).getAbility().useAbility(caster, target);
        caster.setCanAct(false);
    }

    private void cardUsesAttack(ActionsInput action) throws IllegalAccessException {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        var cardAttacker = action.getCardAttacker();
        var cardAttacked = action.getCardAttacked();
        Minion attacker = playedCards[cardAttacker.getX()][cardAttacker.getY()];
        Minion attacked = playedCards[cardAttacked.getX()][cardAttacked.getY()];

        IOHandler.INSTANCE.writeToObject("command", "cardUsesAttack");
        IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacker",
                IOHandler.INSTANCE
                        .createObjectNodeFromObject(action.getCardAttacker()));
        IOHandler.INSTANCE.writeJsonNodeToObject("cardAttacked",
                IOHandler.INSTANCE
                        .createObjectNodeFromObject(action.getCardAttacked()));

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

        boolean tauntInLine = false;
        for (var minion : playedCards[currentPlayerIdx + 1]) {
            if (minion == null) {
                break;
            }
            if (minion.getCard().getType() == CardType.TAUNT) {
                tauntInLine = true;
            }
        }

        if (tauntInLine && attacked.getCard().getType() != CardType.TAUNT) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card is not of type 'Tank'.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        attacker.dealDamage(attacked);
        attacker.setCanAct(false);
    }

    private void placeCard(ActionsInput action) {
        final int currentPlayerIdx = getCurrentPlayerIdx();
        MinionCard cardToPlace = heroes[currentPlayerIdx].getCard(action.getHandIdx());
        if (cardToPlace == null) {
            return;
        }

        IOHandler.INSTANCE.writeToObject("command", "placeCard");

        if (cardToPlace.getMana() > heroes[currentPlayerIdx].getMana()) {
            IOHandler.INSTANCE.writeToObject("command", "placeCard");
            IOHandler.INSTANCE.writeToObject("error",
                    "Not enough mana to place card on table.");
            IOHandler.INSTANCE.writeToObject("handIdx", action.getHandIdx());
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        int x = (cardToPlace.getType() == CardType.GENERIC
                || cardToPlace.getType() == CardType.SPECIAL_BACKLINE)
                ? (currentPlayerIdx == 0 ? P1BACK : P2BACK)
                : (currentPlayerIdx == 0 ? P1FRONT : P2FRONT);
        int y = 0;

        loop:
        for (int i = 0; i < TABLEHEIGHT; i++) {
            if (playedCards[x][i] == null) {
                break loop;
            }
            y++;
        }

        if (y > TABLEWIDTH - 1) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Cannot place card on table since row is full.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        Minion minion = new Minion(cardToPlace, x, y, this,
                currentPlayerIdx);
        heroes[currentPlayerIdx].addMana(-cardToPlace.getMana());
        playedCards[x][y] = minion;
        heroes[currentPlayerIdx].removeCard(action.getHandIdx());
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
            onHeroDeath((Hero) entity);
            return;
        }
        onMinionDeath((Minion) entity);
    }
}
