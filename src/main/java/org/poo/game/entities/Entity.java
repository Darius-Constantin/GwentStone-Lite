package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.poo.game.cards.Card;
import org.poo.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Dispatcher;

public abstract class Entity<T extends Entity<T>> {
    @Getter
    @Setter
    @JsonProperty("health")
    protected int health;

    @Getter
    @Setter
    @JsonIgnore
    protected boolean canAct = true;
    /**
     * All entities will base themselves of a card which will act as its template. To avoid storing
     * multiple references to its attributes (e.g., {@code name}), we only store a reference to
     * the card and access attributes using getters (e.g., {@link Card#getName()}.
     */
    @Getter
    @JsonUnwrapped
    protected final Card card;

    @JsonIgnore
    protected final Game currentGame;
    @Getter
    @JsonIgnore
    protected final int ownerPlayerIdx;

    @Getter
    @JsonIgnore
    protected Dispatcher<T> deathDispatcher = new Dispatcher<>();

    public Entity(final Card card, final Game currentGame, final int ownerPlayerIdx) {
        this.card = card;
        this.health = card.getHealth();
        this.currentGame = currentGame;
        this.ownerPlayerIdx = ownerPlayerIdx;
    }

    /**
     * A function for dealing with the death of an entity. Should primarily be used to notify
     * dependant classes of its death.
     */
    public abstract void kill();

    /**
     * Function for taking damage by an entity that automatically triggers death on falling below
     * 1 health points.
     * @param damage The amount of damage taken by the entity, which will be subtracted from
     * {@link #health}.
     */
    public void takeDamage(final int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            kill();
        }
    }

    /**
     * Default implementation for resetting an entity on the end of the current player's turn.
     */
    public void reset() {
        this.canAct = true;
    }
}
