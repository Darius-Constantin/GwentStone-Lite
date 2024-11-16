package org.poo.game.cards.specialCards;

import org.poo.fileio.IOHandler;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public final class Disciple extends SpecialMinionCard {
    public Disciple(final int health, final int mana, final int attackDamage,
                    final String description, final ArrayList<String> colors, final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_BACKLINE,
                true);
    }

    public Disciple(final SpecialMinionCard card) {
        super(card);
    }

    @Override
    public boolean checkAbilityValidity(final Minion caster, final Minion target) {
        if (caster.getOwnerPlayerIdx() != target.getOwnerPlayerIdx()) {
            IOHandler.getInstance().writeToObject("error",
                    "Attacked card does not belong to the current player.");
            IOHandler.getInstance().writeObjectToOutput();
            return false;
        }
        return true;
    }

    @Override
    public void useAbility(final Minion caster, final Minion target) {
        target.setHealth(target.getHealth() + 2);
    }
}
