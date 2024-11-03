package entities;

import cards.Card;
import cards.MinionCard;
import game.Game;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class Hero extends Entity {
    private final ArrayList<MinionCard> availableCards;
    private final ArrayList<MinionCard> inHandCards;
    private int mana = 0;
    private final int playerIdx;

    public Hero(final Card hero,
                final ArrayList<MinionCard> deck,
                final Game currentGame,
                final int playerIdx) {
        super(hero, currentGame, playerIdx);
        this.availableCards = deck;
        this.inHandCards = new ArrayList<>(deck.size());
        this.playerIdx = playerIdx;
    }

    public void addMana(final int manaToAdd) {
        this.mana += manaToAdd;
    }

    public void drawCard() {
        if (availableCards.isEmpty()) {
            return;
        }
        inHandCards.add(availableCards.remove(0));
    }

    public MinionCard removeCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.remove(idx);
    }

    public MinionCard getCard(final int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size()) {
            return null;
        }
        return inHandCards.get(idx);
    }

    public void reset(final int manaToAdd) {
        super.reset();
        this.mana += manaToAdd;
        drawCard();
    }
}
