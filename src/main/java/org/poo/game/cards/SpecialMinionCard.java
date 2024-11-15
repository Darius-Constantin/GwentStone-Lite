package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.fileio.IOHandler;
import org.poo.game.entities.Minion;

import java.util.ArrayList;

@Getter
public abstract class SpecialMinionCard extends MinionCard {
    @JsonIgnore
    protected final boolean abilityIgnoresTaunt;

    public SpecialMinionCard(int health, int mana, int attackDamage, String description,
                             ArrayList<String> colors, String name, CardType type, final boolean abilityIgnoresTaunt) {
        super(health, mana, attackDamage, description, colors, name, type);
        this.abilityIgnoresTaunt = abilityIgnoresTaunt;
    }

    public SpecialMinionCard(SpecialMinionCard card) {
        super(card);
        this.abilityIgnoresTaunt = card.abilityIgnoresTaunt;
    }

    public boolean checkAbilityValidity(final Minion caster, final Minion target) {
        if (caster.getOwnerPlayerIdx() == target.getOwnerPlayerIdx()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card does not belong to the enemy.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return false;
        }
        return true;
    }

    public abstract void useAbility(final Minion caster, final Minion target);
}
