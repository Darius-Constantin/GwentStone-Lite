package org.poo.game.cards.specialCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public final class TheRipper extends SpecialMinionCard {
    public TheRipper(final int health, final int mana, final int attackDamage,
                     final String description, final ArrayList<String> colors, final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_FRONTLINE,
                false);
    }

    public TheRipper(final SpecialMinionCard card) {
        super(card);
    }

    @Override
    public void useAbility(final Minion caster, final Minion target) {
        target.setAttackDamage(Math.max(target.getAttackDamage() - 2, 0));
    }
}
