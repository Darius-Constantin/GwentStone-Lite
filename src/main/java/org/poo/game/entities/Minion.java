package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.poo.game.cards.MinionCard;
import org.poo.game.Game;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Minion extends Entity {
    @JsonIgnore
    private final int x;
    @JsonIgnore
    private int y;
    @JsonIgnore
    private boolean frozen = false;
    @JsonProperty("attackDamage")
    protected int attackDamage;

    public Minion(final MinionCard card,
                  final int x,
                  final int y,
                  final Game currentGame,
                  final int playerIdx) {
        super(card, currentGame, playerIdx);
        this.attackDamage = card.getAttackDamage();
        this.x = x;
        this.y = y;
    }

    public void dealDamage(final Entity target) {
        target.takeDamage(getAttackDamage());
    }

    public String toString() {
        return "[MINION] " + getCard().getName()
                + " HP = " + health
                + " MANA = " + getCard().getMana()
                + " ATK = " + attackDamage;
    }

    @Override
    public void reset() {
        this.canAct = true;
        this.frozen = false;
    }
}
