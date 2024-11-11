package org.poo.game.cards;

import org.poo.game.entities.Minion;
import org.poo.game.Game;
import lombok.Getter;

@Getter
public enum HeroAbility {
    SUBZERO((Minion[] row) -> {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setFrozen(true);
        }
    }),
    LOWBLOW((Minion[] row) -> {
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
    }),
    EARTHBORN((Minion[] row) -> {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setHealth(row[i].getHealth() + 1);
        }
    }),
    BLOODTHIRST((Minion[] row) -> {
        for (int i = 0; i < Game.TABLE_WIDTH; i++) {
            if (row[i] == null) {
                break;
            }
            row[i].setAttackDamage(row[i].getAttackDamage() + 1);
        }
    });

    interface Ability {
        void ability(Minion[] row);
    }

    private final Ability ability;
    HeroAbility(final Ability ability) {
        this.ability = ability;
    }

    public void useAbility(final Minion[] row) {
        ability.ability(row);
    }
}
