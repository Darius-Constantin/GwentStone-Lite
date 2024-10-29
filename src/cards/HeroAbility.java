package cards;

import entities.Minion;
import game.Game;
import lombok.Getter;

@Getter
public enum HeroAbility {
    SUBZERO((Minion[] row) -> {
        for (int i = 0; i < Game.tableWidth; i++) {
            if (row[i] == null)
                break;
            row[i].setFrozen(true);
        }
    }),
    LOWBLOW((Minion[] row) -> {
        Minion maxHealthMinion = null;
        int maxHealth = 0;
        for (int i = 0; i < Game.tableWidth; i++) {
            if (row[i] == null)
                break;
            if (row[i].getHealth() > maxHealth) {
                maxHealth = row[i].getHealth();
                maxHealthMinion = row[i];
            }
        }
        if (maxHealthMinion != null)
            maxHealthMinion.kill();
    }),
    EARTHBORN((Minion[] row) -> {
        for (int i = 0; i < Game.tableWidth; i++) {
            if (row[i] == null)
                break;
            row[i].setHealth(row[i].getHealth() + 1);
        }
    }),
    BLOODTHIRST((Minion[] row) -> {
        for (int i = 0; i < Game.tableWidth; i++) {
            if (row[i] == null)
                break;
            row[i].setAttackDamage(row[i].getHealth() + 1);
        }
    });

    interface Ability {
        void ability(Minion[] row);
    }

    final private Ability ability;
    HeroAbility(Ability ability) { this.ability = ability; }
}
