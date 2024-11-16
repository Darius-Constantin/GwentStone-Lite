package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

@Getter
public abstract class SpecialMinionCard extends MinionCard {
    @JsonIgnore
    protected final boolean abilityIgnoresTaunt;

    public SpecialMinionCard(final int health, final int mana, final int attackDamage,
                             final String description, final ArrayList<String> colors,
                             final String name, final CardType type,
                             final boolean abilityIgnoresTaunt) {
        super(health, mana, attackDamage, description, colors, name, type);
        this.abilityIgnoresTaunt = abilityIgnoresTaunt;
    }

    public SpecialMinionCard(final SpecialMinionCard card) {
        super(card);
        this.abilityIgnoresTaunt = card.abilityIgnoresTaunt;
    }

    /**
     * Function used to check if this minion's ability can be cast upon its target. By default,
     * both minions must not be part of the same team. Must be overridden in the case of minions
     * whose abilities are applied on allies (such as
     * {@link org.poo.game.cards.specialCards.Disciple}).
     * @param caster The minion that attempts to cast the ability.
     * @param target The minion upon which the ability will be cast.
     * @return {@code null} if the {@code caster} can cast upon {@code target}. An error message otherwise.
     */
    public String checkAbilityValidity(final Minion caster, final Minion target) {
        if (caster.getOwnerPlayerIdx() == target.getOwnerPlayerIdx()) {
            return "Attacked card does not belong to the enemy.";
        }
        return null;
    }

    /**
     * This function needs to be overridden for each SpecialMinionCard to determine the behaviour
     * of its ability.
     * @param caster The minion that attempts to cast the ability.
     * @param target The minion upon which the ability will be cast.
     */
    public abstract void useAbility(Minion caster, Minion target);
}
