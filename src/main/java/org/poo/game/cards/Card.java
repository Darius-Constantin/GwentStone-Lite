package org.poo.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public abstract class Card {
    @JsonProperty("health")
    protected final int health;
    @JsonProperty("mana")
    protected final int mana;
    @JsonProperty("description")
    private final String description;
    @JsonProperty("colors")
    protected final ArrayList<String> colors;
    @JsonProperty("name")
    protected final String name;
    @JsonIgnore
    protected final CardType type;

    public Card(final int health,
                final int mana,
                final String description,
                final ArrayList<String> colors,
                final String name,
                final CardType type) {
        this.health = health;
        this.mana = mana;
        this.description = description;
        this.colors = colors;
        this.name = name;
        this.type = type;
    }

    public Card(final Card card) {
        this.health = card.health;
        this.mana = card.mana;
        this.description = card.description;
        this.colors = card.colors;
        this.name = card.name;
        this.type = card.type;
    }
}
