package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.game.entities.Minion;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public abstract class HeroCard extends Card {
    @JsonIgnore
    static final int STARTING_HEALTH = 30;
    @JsonIgnore
    final boolean abilityAffectEnemy;

    public HeroCard(final int mana,
                    final String description,
                    final ArrayList<String> colors,
                    final String name, boolean abilityAffectEnemy) {
        super(STARTING_HEALTH, mana, description, colors, name, CardType.HERO);
        this.abilityAffectEnemy = abilityAffectEnemy;
    }

    public abstract void useAbility(Minion[] row);
}
