package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Card {
    final public int health;
    final public int mana;
    final public int attackDamage;
    final public String description;
    final public ArrayList<String> colors;
    final public String name;

    public Card(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        this.health = health;
        this.mana = mana;
        this.attackDamage = attackDamage;
        this.description = description;
        this.colors = colors;
        this.name = name;
    }

    public Card(Card card) {
        this.health = card.health;
        this.mana = card.mana;
        this.attackDamage = card.attackDamage;
        this.description = card.description;
        this.colors = card.colors;
        this.name = card.name;
    }

    public static ArrayList<Card> cloneDeck(ArrayList<Card> cardList) {
        ArrayList<Card> clonedList = new ArrayList<Card>(cardList.size());
        for (Card card : cardList) {
            clonedList.add(new Card(card));
        }
        return clonedList;
    }

    public static ArrayList<Card> shuffleDeck(ArrayList<Card> cardList, int seed) {
        Collections.shuffle(cardList, new Random(seed));
        return cardList;
    }
}
