package org.poo.game.cards.specialCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public final class Miraj extends SpecialMinionCard {
    public Miraj(final int health, final int mana, final int attackDamage, final String description,
                 final ArrayList<String> colors, final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_FRONTLINE,
                false);
    }

    public Miraj(final SpecialMinionCard card) {
        super(card);
    }

    @Override
    public void useAbility(final Minion caster, final Minion target) {
        int targetHealth = target.getHealth();
        target.setHealth(caster.getHealth());
        caster.setHealth(targetHealth);
    }
}
