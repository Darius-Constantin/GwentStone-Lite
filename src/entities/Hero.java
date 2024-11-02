package entities;

import cards.Card;
import cards.MinionCard;
import game.Game;
import lombok.Getter;

import java.util.ArrayList;

public class Hero extends Entity {
    @Getter
    private ArrayList<MinionCard> availableCards;
    @Getter
    private ArrayList<MinionCard> inHandCards;
    @Getter
    private int mana = 0;
    final public int playerIdx;

    public Hero(Card hero, ArrayList<MinionCard> deck, Game currentGame, int playerIdx) {
        super(hero, currentGame, playerIdx);
        this.availableCards = deck;
        this.inHandCards = new ArrayList<>(deck.size());
        this.playerIdx = playerIdx;
    }

    public void addMana(int mana) { this.mana += mana; }

    public void drawCard() {
        if (availableCards.isEmpty())
            return;
        inHandCards.add(availableCards.remove(0));
    }

    public MinionCard removeCard(int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size())
            return null;
        return inHandCards.remove(idx);
    }

    public MinionCard getCard(int idx) {
        if (inHandCards.isEmpty() || idx >= inHandCards.size())
            return null;
        return inHandCards.get(idx);
    }
}
