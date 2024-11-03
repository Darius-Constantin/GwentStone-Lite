package cards;

import fileio.SerializeField;
import fileio.SerializeHandler;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public abstract class Card implements SerializeHandler {
    @SerializeField(label = "health")
    private final int health;
    @SerializeField(label = "mana")
    private final int mana;
    @SerializeField(label = "description")
    private final String description;
    @SerializeField(label = "colors")
    private final ArrayList<String> colors;
    @SerializeField(label = "name")
    private final String name;
    private final CardType type;

    public Card(final int health,
                final int mana,
                final String description,
                final ArrayList<String> colors,
                final String name) {
        this.health = health;
        this.mana = mana;
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

    public Card(final Card card) {
        this.health = card.health;
        this.mana = card.mana;
        this.description = card.description;
        this.colors = card.colors;
        this.name = card.name;
        this.type = card.type;
    }
}
