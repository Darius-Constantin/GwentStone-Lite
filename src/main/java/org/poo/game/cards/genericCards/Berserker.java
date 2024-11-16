package org.poo.game.cards.genericCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.MinionCard;

import java.util.ArrayList;

public class Berserker extends MinionCard {
    public Berserker(final int health, final int mana, final int attackDamage,
                     final String description, final ArrayList<String> colors, final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.GENERIC);
    }

    public Berserker(final MinionCard card) {
        super(card);
    }
}
