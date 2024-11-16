package org.poo.game.entities;

import org.poo.fileio.IOHandler;
import org.poo.game.Game;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;

public final class SpecialMinion extends Minion {
    public SpecialMinion(final SpecialMinionCard card, final int x, final int y,
                         final Game currentGame, final int playerIdx) {
        super(card, x, y, currentGame, playerIdx);
    }

    /**
     * Function used to attempt to trigger the use of this minion's ability. If multiple checks
     * fail (such as checking if the minion hasn't already acted this turn), the ability will not
     * be used.
     * @param target The target upon which the ability will be cast.
     */
    public void useAbility(final Minion target) {
        if (this.canMinionAct()) {
            return;
        }

        if (!((SpecialMinionCard) this.card).checkAbilityValidity(this, target)) {
            return;
        }

        if (!((SpecialMinionCard) this.card).isAbilityIgnoresTaunt()) {
            if (this.currentGame.isTauntOnEnemySide()
                    && target.getCard().getType() != CardType.TAUNT) {
                IOHandler.getInstance().writeToObject("error",
                        "Attacked card is not of type 'Tank'.");
                IOHandler.getInstance().writeObjectToOutput();
                return;
            }
        }

        ((SpecialMinionCard) this.card).useAbility(this, target);
        this.setCanAct(false);
    }
}
