package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.fileio.IOHandler;
import org.poo.game.Game;
import org.poo.game.cards.Card;
import org.poo.game.cards.HeroCard;
import org.poo.game.cards.MinionCard;

import java.util.ArrayList;

@Getter
public final class Hero extends Entity {
    @JsonIgnore
    private final ArrayList<MinionCard> availableCards;
    @JsonIgnore
    private final ArrayList<MinionCard> inHandCards;
    @JsonIgnore
    private final int playerIdx;
    @JsonIgnore
    private int mana = 0;

    public Hero(final Card hero, final ArrayList<MinionCard> deck, final Game currentGame,
                final int playerIdx) {
        super(hero, currentGame, playerIdx);
        this.availableCards = deck;
        this.inHandCards = new ArrayList<>(deck.size());
        this.playerIdx = playerIdx;
    }

    /**
     * Function that notifies the game that this hero has died.
     */
    @Override
    public void kill() {
        currentGame.onHeroDeath();
    }

    /**
     * Function to add (or subtract) mana to (from) this hero.
     *
     * @param manaToAdd The amount of mana to add to the hero. If the value is negative, mana will
     *                  be subtracted which is useful when it places cards or uses its ability.
     */
    public void addMana(final int manaToAdd) {
        this.mana += manaToAdd;
    }

    /**
     * Function for drawing a card from the hero's deck. If the deck has been depleted, no more
     * cards are drawn.
     */
    public void drawCard() {
        if (availableCards.isEmpty()) {
            return;
        }
        inHandCards.add(availableCards.removeFirst());
    }

    /**
     * Function for removing a card specified by its index from the hero's hand.
     *
     * @param idx The index of the card which is to be removed.
     * @return The removed card. Can be useful if the card needs to be reintroduced in the deck
     * or if future development of cards require stealing from the enemy's deck.
     */
    public MinionCard removeCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.remove(idx);
    }

    /**
     * Function for retrieving a card specified by its index from the hero's hand.
     *
     * @param idx The index of the card which is to be removed.
     * @return The card found at the given index. If no such index exists, returns {@code null}.
     */
    public MinionCard getCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.get(idx);
    }

    /**
     * Function to reset the state of a hero by allowing it to act again next turn, adding mana,
     * and drawing another card from the deck.
     */
    public void reset(final int manaToAdd) {
        super.reset();
        this.mana += manaToAdd;
        drawCard();
    }

    /**
     * Function used to check if the row at the given index belongs to this hero or to the enemy.
     *
     * @param rowIdx The index of the row whose owner is required.
     * @return True if the supplied row belongs to the enemy. False otherwise.
     */
    public boolean isEnemyRow(final int rowIdx) {
        return (playerIdx == 0 && rowIdx <= 1) || (playerIdx == 1 && rowIdx >= 2);
    }

    /**
     * Function used to attempt to trigger the use of this hero's ability. If multiple checks
     * fail (such as checking if the hero hasn't already acted this turn), the ability will not
     * be used.
     *
     * @param row The row which will be affected by the hero's ability.
     * @param rowIdx The index of the row, used to check belonging to the hero.
     */
    public void useAbility(final Minion[] row, final int rowIdx) {
        if (card.getMana() > this.mana) {
            IOHandler.getInstance().writeToObject("error",
                    "Not enough mana to use hero's ability.");
            IOHandler.getInstance().writeObjectToOutput();
            return;
        }

        if (!this.canAct) {
            IOHandler.getInstance().writeToObject("error",
                    "Hero has already attacked this turn.");
            IOHandler.getInstance().writeObjectToOutput();
            return;
        }

        if (((HeroCard) card).isAbilityAffectEnemy() && !isEnemyRow(rowIdx)) {
            IOHandler.getInstance().writeToObject("error",
                    "Selected row does not belong to the enemy.");
            IOHandler.getInstance().writeObjectToOutput();
            return;
        }

        if (!((HeroCard) card).isAbilityAffectEnemy() && isEnemyRow(rowIdx)) {
            IOHandler.getInstance().writeToObject("error",
                    "Selected row does not belong to the current player.");
            IOHandler.getInstance().writeObjectToOutput();
            return;
        }

        addMana(-1 * card.getMana());
        ((HeroCard) card).useAbility(row);
        this.setCanAct(false);
    }
}
