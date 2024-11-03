package cards;

import entities.Minion;

public enum MinionAbility {
    MINION((Minion caster, Minion target) -> { }),
    WEAKKNEES((Minion caster, Minion target) -> {
        target.setAttackDamage(Math.max(target.getAttackDamage() - 2, 0));
    }),
    SKYJACK((Minion caster, Minion target) -> {
        int targetHealth = target.getHealth();
        target.setHealth(caster.getHealth());
        caster.setHealth(targetHealth);
    }),
    SHAPESHIFT((Minion caster, Minion target) -> {
        if (target.getAttackDamage() == 0) {
            target.kill();
        }
        int targetHealth = target.getHealth();
        target.setHealth(target.getAttackDamage());
        target.setAttackDamage(targetHealth);
    }),
    GODSPLAN((Minion caster, Minion target) -> {
        target.setHealth(target.getHealth() + 2);
    });

    interface Ability {
        void ability(Minion caster, Minion target);
    }

    private final Ability ability;
    MinionAbility(final Ability ability) {
        this.ability = ability;
    }
    public void useAbility(final Minion caster, final Minion target) {
        ability.ability(caster, target);
    }
}
