package cards;

import entities.Minion;
import lombok.Getter;

@Getter
public enum MinionAbility {
    MINION((Minion caster, Minion target) -> {}),
    WEAKKNEES((Minion caster, Minion target) -> {
        if (target.getAttackDamage() > 2)
            target.setAttackDamage(target.getAttackDamage() - 2);
        else
            target.setAttackDamage(0);
    }),
    SKYJACK((Minion caster, Minion target) -> {
        int targetHealth = target.getHealth();
        target.setHealth(caster.getHealth());
        caster.setHealth(targetHealth);
    }),
    SHAPESHIFT((Minion caster, Minion target) -> {
        int targetHealth = target.getHealth();
        target.setHealth(target.getAttackDamage());
        target.setAttackDamage(targetHealth);
        if (target.getHealth() == 0)
            target.kill();
    }),
    GODSPLAN((Minion caster, Minion target) -> {
        target.setHealth(target.getHealth() + 2);
    });

    interface Ability {
        void ability(Minion caster, Minion target);
    }

    private final Ability ability;
    MinionAbility(Ability ability) { this.ability = ability; }
}
