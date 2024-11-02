package entities;

import cards.Card;
import cards.MinionCard;
import game.Game;
import lombok.Getter;
import lombok.Setter;

public class Minion extends Entity {
    @Setter
    @Getter
    private int x;
    @Setter
    @Getter
    private int y;
    @Setter
    @Getter
    private boolean frozen = false;

    public Minion(Card card, int x, int y, Game currentGame, int playerIdx) {
        super(card, currentGame, playerIdx);
        this.x = x;
        this.y = y;
    }

    public void dealDamage(Entity target) {
        target.takeDamage(getAttackDamage());
    }

    public String toString() {
        return "[MINION] " + card.name + " HP = " + health + " MANA = " + card.mana + " ATK = " + attackDamage;
    }
}
