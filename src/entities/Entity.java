package entities;

import cards.Card;
import fileio.SerializableField;
import fileio.SerializeField;
import fileio.SerializeHandler;
import game.Game;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Entity implements SerializeHandler {
    @Getter
    @Setter
    @SerializeField(label = "health")
    protected int health;

    @Getter
    @Setter
    protected boolean canAct = true;
    final public Card card;

    final public Game currentGame;
    final public int ownerPlayerIdx;

    public Entity(Card card, Game currentGame, int ownerPlayerIdx) {
        this.card = card;
        this.health = card.health;
        this.currentGame = currentGame;
        this.ownerPlayerIdx = ownerPlayerIdx;
    }

    public void kill() {
        currentGame.onEntityDeath(this);
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0)
            kill();
    }

    @Override
    public ArrayList<SerializableField> getSerializableFields() throws IllegalAccessException {
        ArrayList<SerializableField> fields = SerializeHandler.super.getSerializableFields();
        fields.addAll(card.getSerializableFields());
        return fields;
    }
}
