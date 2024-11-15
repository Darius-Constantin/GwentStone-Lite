package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.poo.fileio.IOHandler;
import org.poo.game.cards.Card;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionCard;
import org.poo.game.Game;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class Hero extends Entity {
    @JsonIgnore
    private final ArrayList<MinionCard> availableCards;
    @JsonIgnore
    private final ArrayList<MinionCard> inHandCards;
    @JsonIgnore
    private int mana = 0;
    @JsonIgnore
    private final int playerIdx;

    public Hero(final Card hero,
                final ArrayList<MinionCard> deck,
                final Game currentGame,
                final int playerIdx) {
        super(hero, currentGame, playerIdx);
        this.availableCards = deck;
        this.inHandCards = new ArrayList<>(deck.size());
        this.playerIdx = playerIdx;
    }

    public void addMana(final int manaToAdd) {
        this.mana += manaToAdd;
    }

    public void drawCard() {
        if (availableCards.isEmpty()) {
            return;
        }
        inHandCards.add(availableCards.removeFirst());
    }

    public MinionCard removeCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.remove(idx);
    }

    public MinionCard getCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.get(idx);
    }

    public void reset(final int manaToAdd) {
        super.reset();
        this.mana += manaToAdd;
        drawCard();
    }

    public boolean isEnemyRow(final int rowIdx) {
        return (playerIdx == 0 && rowIdx <= 1) || (playerIdx == 1 && rowIdx >= 2);
    }

    public void useAbility(final Minion[] row, final int rowIdx) {
        if (card.getMana() > this.mana) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Not enough mana to use hero's ability.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (!this.canAct) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Hero has already attacked this turn.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (((HeroCard) card).isAbilityAffectEnemy() && !isEnemyRow(rowIdx)) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Selected row does not belong to the enemy.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (!((HeroCard) card).isAbilityAffectEnemy() && isEnemyRow(rowIdx)) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Selected row does not belong to the current player.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        addMana(-1 * card.getMana());
        ((HeroCard) card).useAbility(row);
        this.setCanAct(false);
    }
}
