package cards;

import java.util.ArrayList;

public class HeroCard extends Card {
    final public HeroAbility ability;

    public HeroCard(int mana, int attackDamage, String description, ArrayList<String> colors, String name) {
        super(30, mana, attackDamage, description, colors, name);

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
