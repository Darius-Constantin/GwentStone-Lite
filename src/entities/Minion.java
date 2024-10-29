package entities;

import cards.Card;
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

    public void dealDamage(int damage, Entity target) {
        target.takeDamage(damage);
    }
}
