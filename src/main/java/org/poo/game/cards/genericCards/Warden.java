package org.poo.game.cards.genericCards;

import org.poo.game.cards.CardType;
import org.poo.game.cards.MinionCard;

import java.util.ArrayList;

public class Warden extends MinionCard {

    public Warden(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(health, mana, attackDamage, description, colors, name, CardType.TAUNT);
    }

    public Warden(MinionCard card) {
        super(card);
    }
}
