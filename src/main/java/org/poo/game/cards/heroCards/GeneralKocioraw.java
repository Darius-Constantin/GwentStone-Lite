package org.poo.game.cards.heroCards;

import org.poo.game.Game;
import org.poo.game.cards.HeroCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class GeneralKocioraw extends HeroCard {
    public GeneralKocioraw(int mana, String description, ArrayList<String> colors, String name) {
        super(mana, description, colors, name, false);
    }

    @Override
    public void useAbility(Minion[] row) {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setAttackDamage(row[i].getAttackDamage() + 1);
        }
    }
}
