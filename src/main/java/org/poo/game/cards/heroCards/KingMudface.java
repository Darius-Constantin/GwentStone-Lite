package org.poo.game.cards.heroCards;

import org.poo.game.Game;
import org.poo.game.cards.HeroCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public final class KingMudface extends HeroCard {
    public KingMudface(final int mana, final String description, final ArrayList<String> colors,
                       final String name) {
        super(mana, description, colors, name, false);
    }

    @Override
    public void useAbility(final Minion[] row) {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setHealth(row[i].getHealth() + 1);
        }
    }
}
