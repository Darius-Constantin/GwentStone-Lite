package org.poo.game.cards.specialCards;

import org.poo.fileio.IOHandler;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class TheCursedOne extends SpecialMinionCard {
    public TheCursedOne(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_BACKLINE,
                false);
    }

    public TheCursedOne(SpecialMinionCard card) {
        super(card);
    }

    @Override
    public void useAbility(Minion caster, Minion target) {
        if (target.getAttackDamage() == 0) {
            target.kill();
        }
        int targetHealth = target.getHealth();
        target.setHealth(target.getAttackDamage());
        target.setAttackDamage(targetHealth);
    }
}
