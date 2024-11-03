package entities;

import cards.Card;
import fileio.SerializableField;
import fileio.SerializeField;
import fileio.SerializeHandler;
import game.Game;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Entity implements SerializeHandler {
    @Getter
    @Setter
    @SerializeField(label = "health")
    protected int health;

    @Getter
    @Setter
    protected boolean canAct = true;
    @Getter
    private final Card card;

    private final Game currentGame;
    @Getter
    private final int ownerPlayerIdx;

    public Entity(final Card card,
                  final Game currentGame,
                  final int ownerPlayerIdx) {
        this.card = card;
        this.health = card.getHealth();
        this.currentGame = currentGame;
        this.ownerPlayerIdx = ownerPlayerIdx;
    }

    public void kill() {
        currentGame.onEntityDeath(this);
    }

    public void takeDamage(final int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            kill();
        }
    }

    @Override
    public ArrayList<SerializableField> getSerializableFields() throws IllegalAccessException {
        ArrayList<SerializableField> fields = SerializeHandler.super.getSerializableFields();
        ArrayList<SerializableField> cardFields = card.getSerializableFields();
        cardFields.removeIf(field -> (field.getLabel().equals("attackDamage")
                || field.getLabel().equals("health")));
        fields.addAll(cardFields);
        return fields;
    }

    public void reset() {
        this.canAct = true;
    }
}
