package org.poo.cards;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class HeroCard extends Card {
    private final HeroAbility ability;
    static final int STARTINGHEALTH = 30;

    public HeroCard(final int mana,
                    final String description,
                    final ArrayList<String> colors,
                    final String name) {
        super(STARTINGHEALTH, mana, description, colors, name);

        switch (name) {
            case "Lord Royce":
                ability = HeroAbility.SUBZERO;
                break;
            case "Empress Thorina":
                ability = HeroAbility.LOWBLOW;
                break;
            case "King Mudface":
                ability = HeroAbility.EARTHBORN;
                break;
            case "General Kocioraw":
                ability = HeroAbility.BLOODTHIRST;
                break;
            default:
                throw new RuntimeException("Invalid card.");
        }
    }
}
