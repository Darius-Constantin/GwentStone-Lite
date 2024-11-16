package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public abstract class MinionCard extends Card {
    @JsonProperty("attackDamage")
    private final int attackDamage;

    public MinionCard(final int health, final int mana, final int attackDamage,
                      final String description, final ArrayList<String> colors, final String name,
                      final CardType type) {
        super(health, mana, description, colors, name, type);
        this.attackDamage = attackDamage;
    }

    public MinionCard(final MinionCard card) {
        super(card);
        this.attackDamage = card.attackDamage;
    }
}
