package org.poo.game.cards.specialCards;

import org.poo.fileio.IOHandler;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class Disciple extends SpecialMinionCard {
    public Disciple(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_BACKLINE,
                true);
    }

    public Disciple(SpecialMinionCard card) {
        super(card);
    }

    @Override
    public boolean checkAbilityValidity(Minion caster, Minion target) {
        if (caster.getOwnerPlayerIdx() != target.getOwnerPlayerIdx()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card does not belong to the current player.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return false;
        }
        return true;
    }

    @Override
    public void useAbility(Minion caster, Minion target) {
        target.setHealth(target.getHealth() + 2);
    }
}
