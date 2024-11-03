package entities;

import cards.Card;
import fileio.SerializableField;
import fileio.SerializeField;
import game.Game;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Setter
@Getter
public class Minion extends Entity {
    private int x;
    private int y;
    private boolean frozen = false;
    @SerializeField(label = "attackDamage")
    protected int attackDamage;

    public Minion(Card card, int x, int y, Game currentGame, int playerIdx) {
        super(card, currentGame, playerIdx);
        this.attackDamage = card.attackDamage;
        this.x = x;
        this.y = y;
    }

    public void dealDamage(Entity target) {
        target.takeDamage(getAttackDamage());
    }

    public String toString() {
        return "[MINION] " + card.name + " HP = " + health + " MANA = " + card.mana + " ATK = " + attackDamage;
    }

    @Override
    public ArrayList<SerializableField> getSerializableFields() throws IllegalAccessException {
        ArrayList<SerializableField> fields = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields())
            if (field.isAnnotationPresent(SerializeField.class))
                fields.add(new SerializableField(field.getAnnotation(SerializeField.class).label(), field.get(this)));

        ArrayList<SerializableField> superFields = super.getSerializableFields();
        superFields.addAll(fields);
        return superFields;
    }
}
