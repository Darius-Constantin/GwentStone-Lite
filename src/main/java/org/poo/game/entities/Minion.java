package org.poo.game.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.poo.fileio.IOHandler;
import org.poo.game.cards.CardType;
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

    @Override
    public void reset() {
        this.canAct = true;
        this.frozen = false;
    }

    public boolean hasMinionActed() {
        if (!this.isCanAct()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card has already attacked this turn.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return true;
        }

        if (this.isFrozen()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacker card is frozen.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return true;
        }

        return false;
    }

    public void attack(final Entity target) {
        if (this.getOwnerPlayerIdx() == target.getOwnerPlayerIdx()) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card does not belong to the enemy.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        if (this.hasMinionActed())
            return;

        if (currentGame.isTauntOnEnemySide() && target.getCard().getType() != CardType.TAUNT) {
            IOHandler.INSTANCE.writeToObject("error",
                    "Attacked card is not of type 'Tank'.");
            IOHandler.INSTANCE.writeObjectToOutput();
            return;
        }

        this.dealDamage(target);
        this.setCanAct(false);
    }
}
