package org.poo.game.entities;

import org.poo.fileio.IOHandler;
import org.poo.game.Game;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;

public class SpecialMinion extends Minion {
    public SpecialMinion(SpecialMinionCard card, int x, int y, Game currentGame, int playerIdx) {
        super(card, x, y, currentGame, playerIdx);
    }

    public void useAbility(Minion target) {
        if (this.hasMinionActed()) {
            return;
        }

        if (!((SpecialMinionCard)this.card).checkAbilityValidity(this, target)) {
            return;
        }

        if(!((SpecialMinionCard)this.card).isAbilityIgnoresTaunt()) {
            if (this.currentGame.isTauntOnEnemySide() && target.getCard().getType() != CardType.TAUNT) {
                IOHandler.INSTANCE.writeToObject("error",
                        "Attacked card is not of type 'Tank'.");
                IOHandler.INSTANCE.writeObjectToOutput();
                return;
            }
        }

        ((SpecialMinionCard)this.card).useAbility(this, target);
        this.setCanAct(false);
    }
}
