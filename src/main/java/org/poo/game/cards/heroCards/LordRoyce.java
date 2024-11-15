package org.poo.game.cards.heroCards;

import org.poo.game.Game;
import org.poo.game.cards.HeroCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class LordRoyce extends HeroCard {
    public LordRoyce(int mana, String description, ArrayList<String> colors, String name) {
        super(mana, description, colors, name, true);
    }

    @Override
    public void useAbility(Minion[] row) {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setFrozen(true);
        }
    }
}
