package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.poo.game.cards.Card;
import org.poo.game.Game;
import lombok.Getter;
import lombok.Setter;

public class Entity {
    @Getter
    @Setter
    @JsonProperty("health")
    protected int health;

    @Getter
    @Setter
    @JsonIgnore
    protected boolean canAct = true;
    @Getter
    @JsonUnwrapped
    private final Card card;

    @JsonIgnore
    private final Game currentGame;
    @Getter
    @JsonIgnore
    private final int ownerPlayerIdx;

    public Entity(final Card card,
                  final Game currentGame,
                  final int ownerPlayerIdx) {
        this.card = card;
        this.health = card.getHealth();
        this.currentGame = currentGame;
        this.ownerPlayerIdx = ownerPlayerIdx;
    }

    public void kill() {
        currentGame.onEntityDeath(this);
    }

    public void takeDamage(final int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            kill();
        }
    }

    public void reset() {
        this.canAct = true;
    }
}
