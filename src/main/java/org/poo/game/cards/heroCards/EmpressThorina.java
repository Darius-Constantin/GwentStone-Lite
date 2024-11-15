package org.poo.game.cards.heroCards;

import org.poo.game.Game;
import org.poo.game.cards.HeroCard;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

public class EmpressThorina extends HeroCard {
    public EmpressThorina(int mana, String description, ArrayList<String> colors, String name) {
        super(mana, description, colors, name, true);
    }

    @Override
    public void useAbility(Minion[] row) {
        Minion maxHealthMinion = null;
        int maxHealth = 0;
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            if (row[i].getHealth() > maxHealth) {
                maxHealth = row[i].getHealth();
                maxHealthMinion = row[i];
            }
        }
        if (maxHealthMinion != null) {
            maxHealthMinion.kill();
        }
    }
}
