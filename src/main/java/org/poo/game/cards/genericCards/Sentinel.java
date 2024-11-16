package org.poo.game.cards.genericCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.MinionCard;

import java.util.ArrayList;

public class Sentinel extends MinionCard {

    public Sentinel(final int health, final int mana, final int attackDamage,
                    final String description, final ArrayList<String> colors, final String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.GENERIC);
    }

    public Sentinel(final MinionCard card) {
        super(card);
    }
}
