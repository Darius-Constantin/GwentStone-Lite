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

    public Minion(final MinionCard card, final int x, final int y, final Game currentGame,
                  final int playerIdx) {
        super(card, currentGame, playerIdx);
        this.attackDamage = card.getAttackDamage();
        this.x = x;
        this.y = y;
    }

    /**
     * Function that notifies the game that this minion has died.
     */
    @Override
    public final void kill() {
        currentGame.onMinionDeath(this);
    }

    /**
     * Opposite function of {@link Entity#takeDamage(int)} in the sense that it will force a given
     * target to take damage equal to {@link #attackDamage}.
     * @param target The target which will be damaged.
     */
    public final void dealDamage(final Entity target) {
        target.takeDamage(attackDamage);
    }

    /**
     * Function to reset the state of a minion by unfreezing and allowing it to act again next turn.
     */
    @Override
    public final void reset() {
        this.canAct = true;
        this.frozen = false;
    }

    /**
     * Function to determine if this minion can act. A minion can act only if it hasn't already
     * acted this turn and if it is not frozen.
     * @return {@code null} if the minion can act. An error message otherwise.
     */
    public final String canMinionAct() {
        if (!this.isCanAct()) {
            return "Attacker card has already attacked this turn.";
        }

        if (this.isFrozen()) {
            return "Attacker card is frozen.";
        }

        return null;
    }

    /**
     * Function used to attempt to attack a minion. If multiple checks fail (such as checking if
     * the minion hasn't already acted this turn), the attack will not be triggered.
     * @param target The target of the attack who might take damage.
     * @return {@code null} if the attack was successful. An error message otherwise.
     */
    public final String attack(final Entity target) {
        if (this.getOwnerPlayerIdx() == target.getOwnerPlayerIdx()) {
            return "Attacked card does not belong to the enemy.";
        }

        String err = this.canMinionAct();
        if (err != null) {
            return err;
        }

        if (currentGame.isTauntOnEnemySide() && target.getCard().getType() != CardType.TAUNT) {
            return "Attacked card is not of type 'Tank'.";
        }

        this.dealDamage(target);
        this.setCanAct(false);
        return null;
    }
}
