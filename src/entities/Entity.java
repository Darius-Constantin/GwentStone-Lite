package entities;

import cards.Card;
import game.Game;
import lombok.Getter;
import lombok.Setter;

public class Entity {
    @Getter
    @Setter
    protected int health;
    @Getter
    @Setter
    protected int attackDamage;
    protected boolean canAct = true;
    final public Card card;

    final public Game currentGame;
    final public int ownerPlayerIdx;

    public Entity(Card card, Game currentGame, int ownerPlayerIdx) {
        this.card = card;
        this.health = card.health;
        this.attackDamage = card.attackDamage;
        this.currentGame = currentGame;
        this.ownerPlayerIdx = ownerPlayerIdx;
    }

    public void kill() {
        currentGame.onEntityDeath(this);
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0)
            kill();
    }
}
