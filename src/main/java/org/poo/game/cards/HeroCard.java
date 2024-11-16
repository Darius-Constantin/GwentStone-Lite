package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

@Getter
public abstract class HeroCard extends Card {
    @JsonIgnore
    static final int STARTING_HEALTH = 30;
    @JsonIgnore
    protected final boolean abilityAffectEnemy;

    public HeroCard(final int mana, final String description, final ArrayList<String> colors,
                    final String name, final boolean abilityAffectEnemy) {
        super(STARTING_HEALTH, mana, description, colors, name, CardType.HERO);
        this.abilityAffectEnemy = abilityAffectEnemy;
    }

    /**
     * This function needs to be overridden for each HeroCard to determine the behaviour
     * of its ability.
     * @param row The row which will be affected by the hero's ability.
     */
    public abstract void useAbility(Minion[] row);
}
