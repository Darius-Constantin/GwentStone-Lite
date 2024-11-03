package cards;

import entities.Minion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MinionCard extends Card {
    final public MinionAbility ability;

    public MinionCard(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(health, mana, attackDamage, description, colors, name);
        switch (name) {
            case "Sentinel":
            case "Berserker":
            case "Goliath":
            case "Warden":
                ability = MinionAbility.MINION;
                break;
            case "The Ripper":
                ability = MinionAbility.WEAKKNEES;
                break;
            case "Miraj":
                ability = MinionAbility.SKYJACK;
                break;
            case "Disciple":
                ability = MinionAbility.GODSPLAN;
                break;
            case "The Cursed One":
                ability = MinionAbility.SHAPESHIFT;
                break;
            default:
                throw new RuntimeException("Invalid card.");
        }
    }

    public MinionCard(MinionCard card) {
        super(card);
        ability = card.ability;
    }

    public static ArrayList<MinionCard> cloneDeck(ArrayList<MinionCard> cardList) {
        ArrayList<MinionCard> clonedList = new ArrayList<MinionCard>(cardList.size());
        for (MinionCard card : cardList) {
            clonedList.add(new MinionCard(card));
        }
        return clonedList;
    }

    public static ArrayList<MinionCard> shuffleDeck(ArrayList<MinionCard> cardList, int seed) {
        Collections.shuffle(cardList, new Random(seed));
        return cardList;
    }

    public String toString() {
        return "[CARD] " + this.name + ": HP = " + this.health + " - MANA = " + this.mana;
    }
}
