package org.poo.game.cards.specialCards;

import org.poo.fileio.IOHandler;
import org.poo.game.cards.CardType;
import org.poo.game.cards.SpecialMinionCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class TheRipper extends SpecialMinionCard {
    public TheRipper(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.SPECIAL_FRONTLINE,
                false);
    }

    public TheRipper(SpecialMinionCard card) {
        super(card);
    }

    @Override
    public void useAbility(Minion caster, Minion target) {
        target.setAttackDamage(Math.max(target.getAttackDamage() - 2, 0));
    }
}
