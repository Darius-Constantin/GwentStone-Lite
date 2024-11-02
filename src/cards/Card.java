package cards;

import java.util.ArrayList;

abstract public class Card {
    final public int health;
    final public int mana;
    final public int attackDamage;
    final public String description;
    final public ArrayList<String> colors;
    final public String name;
    final public CardType type;

    public Card(int health, int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        this.health = health;
        this.mana = mana;
        this.attackDamage = attackDamage;
        this.description = description;
        this.colors = colors;
        this.name = name;

        switch (name) {
            case "Sentinel":
            case "Berserker":
                type = CardType.GENERIC;
                break;
            case "Goliath":
            case "Warden":
                type = CardType.TAUNT;
                break;
            case "Disciple":
            case "The Cursed One":
                type = CardType.SPECIAL_BACKLINE;
                break;
            case "The Ripper":
            case "Miraj":
                type = CardType.SPECIAL_FRONTLINE;
                break;
            case "Lord Royce":
            case "Empress Thorina":
            case "King Mudface":
            case "General Kocioraw":
                type = CardType.HERO;
                break;
            default:
                throw new RuntimeException("Invalid card.");
        }
    }

    public Card(Card card) {
        this.health = card.health;
        this.mana = card.mana;
        this.attackDamage = card.attackDamage;
        this.description = card.description;
        this.colors = card.colors;
        this.name = card.name;
        this.type = card.type;
    }
}
