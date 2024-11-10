package org.poo.cards;

import org.poo.fileio.SerializeField;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@Getter
public class MinionCard extends Card {
    private final MinionAbility ability;
    @SerializeField(label = "attackDamage")
    private final int attackDamage;

    public MinionCard(final int health,
                      final int mana,
                      final int attackDamage,
                      final String description,
                      final ArrayList<String> colors,
                      final String name) {
        super(health, mana, description, colors, name);
        this.attackDamage = attackDamage;
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

    public MinionCard(final MinionCard card) {
        super(card);
        this.attackDamage = card.attackDamage;
        ability = card.ability;
    }

    public static ArrayList<MinionCard> cloneDeck(final ArrayList<MinionCard> cardList) {
        ArrayList<MinionCard> clonedList = new ArrayList<MinionCard>(cardList.size());
        for (MinionCard card : cardList) {
            clonedList.add(new MinionCard(card));
        }
        return clonedList;
    }

    public static ArrayList<MinionCard> shuffleDeck(final ArrayList<MinionCard> cardList,
                                                    final int seed) {
        Collections.shuffle(cardList, new Random(seed));
        return cardList;
    }

    public String toString() {
        return "[CARD] " + this.getName()
                + ": HP = " + this.getHealth()
                + " - MANA = " + this.getMana();
    }
}
