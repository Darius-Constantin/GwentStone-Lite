package org.poo.game.entities;

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
     * @return {@code null} if the ability was successfully used. An error message otherwise.
     */
    public String useAbility(final Minion target) {
        String err = this.canMinionAct();
        if (err != null) {
            return err;
        }

        err = ((SpecialMinionCard) this.card).checkAbilityValidity(this, target);
        if (err != null) {
            return err;
        }

        if (!((SpecialMinionCard) this.card).isAbilityIgnoresTaunt()) {
            if (this.currentGame.isTauntOnEnemySide()
                    && target.getCard().getType() != CardType.TAUNT) {
                return "Attacked card is not of type 'Tank'.";
            }
        }

        ((SpecialMinionCard) this.card).useAbility(this, target);
        this.setCanAct(false);
        return null;
    }
}
