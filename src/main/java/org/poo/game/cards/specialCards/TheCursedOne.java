package org.poo.game.cards.specialCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public final class TheCursedOne extends SpecialMinionCard {
    public TheCursedOne(final int health, final int mana, final int attackDamage,
                        final String description, final ArrayList<String> colors,
                        final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_BACKLINE,
                false);
    }

    public TheCursedOne(final SpecialMinionCard card) {
        super(card);
    }

    @Override
    public void useAbility(final Minion caster, final Minion target) {
        if (target.getAttackDamage() == 0) {
            target.kill();
        }
        int targetHealth = target.getHealth();
        target.setHealth(target.getAttackDamage());
        target.setAttackDamage(targetHealth);
    }
}
