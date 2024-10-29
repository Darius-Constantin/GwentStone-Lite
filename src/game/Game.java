package game;

import cards.Card;
import cards.CardType;
import cards.MinionCard;
import entities.Hero;
import entities.Entity;
import entities.Minion;
import fileio.ActionsInput;
import fileio.IOHandler;
import lombok.Getter;

import java.util.ArrayList;

public class Game  {
    public final Session currentSession;
    public final int seed;
    public final int startingPlayer;
    private final Hero[] heroes = new Hero[2];
    private final ArrayList<ActionsInput> actions;

    public static final int tableWidth = 5;
    public static final int tableHeight = 4;
    private final Minion[][] playedCards = new Minion[tableHeight][tableWidth];
    @Getter
    private int totalTurns = 0;
    private boolean isGameOver = false;

    public Game(Session currentSession, int seed, int startingPlayer, ArrayList<MinionCard> player1Deck,
                ArrayList<MinionCard> player2Deck, Card player1HeroCard, Card player2HeroCard,
                ArrayList<ActionsInput> actions) {
        this.currentSession = currentSession;
        this.seed = seed;
        this.startingPlayer = startingPlayer;
        heroes[0] = new Hero(player1HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player1Deck), seed), this, 0);
        heroes[1] = new Hero(player2HeroCard, MinionCard.shuffleDeck(MinionCard.cloneDeck(player2Deck), seed), this, 1);
        this.actions = actions;
    }

    public int getCurrentTurn() {
        return totalTurns / 2 + 1;
    }

    public int getCurrentPlayerIdx() {
        return totalTurns % 2 == 0 ? startingPlayer : (startingPlayer > 0 ? 0 : 1);
    }

    public void playGame() {
        for (int i = 0; i < 2; i++) {
            heroes[i].addMana(getCurrentTurn());
            heroes[i].drawCard();
        }
        for (ActionsInput action : actions) {
            switch(action.getCommand()) {
                case "getPlayerDeck": {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().beginArray();
                    for (MinionCard card : selectedHero.getAvailableCards()) {
                        IOHandler.getInstance().beginObject();
                        IOHandler.getInstance().writeToObject("mana", card.mana);
                        IOHandler.getInstance().writeToObject("attackDamage", card.attackDamage);
                        IOHandler.getInstance().writeToObject("health", card.health);
                        IOHandler.getInstance().writeToObject("description", card.description);
                        IOHandler.getInstance().beginArray();
                        for (String color : card.colors) {
                            IOHandler.getInstance().writeToArray(color);
                        }
                        IOHandler.getInstance().writeArrayToObject("colors");
                        IOHandler.getInstance().endArray();
                        IOHandler.getInstance().writeToObject("name", card.name);
                        IOHandler.getInstance().writeObjectToArray();
                        IOHandler.getInstance().endObject();
                    }
                    IOHandler.getInstance().writeArrayToObject("output");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerHero": {
                    Hero selectedHero = heroes[action.getPlayerIdx() - 1];
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("playerIdx", action.getPlayerIdx());
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("mana", selectedHero.card.mana);
                    IOHandler.getInstance().writeToObject("description", selectedHero.card.description);
                    IOHandler.getInstance().beginArray();
                    for (String color : selectedHero.card.colors) {
                        IOHandler.getInstance().writeToArray(color);
                    }
                    IOHandler.getInstance().writeArrayToObject("colors");
                    IOHandler.getInstance().endArray();
                    IOHandler.getInstance().writeToObject("name", selectedHero.card.name);
                    IOHandler.getInstance().writeToObject("health", selectedHero.getHealth());
                    IOHandler.getInstance().writeObjectToObject("output");
                    IOHandler.getInstance().endObject();
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "getPlayerTurn": {
                    IOHandler.getInstance().beginObject();
                    IOHandler.getInstance().writeToObject("command", action.getCommand());
                    IOHandler.getInstance().writeToObject("output", getCurrentPlayerIdx() + 1);
                    IOHandler.getInstance().writeObjectToOutput();
                    IOHandler.getInstance().endObject();
                    break;
                }
                case "endPlayerTurn": {
                    totalTurns++;
                    if (getCurrentPlayerIdx() != startingPlayer) {
                        for (int i = 0; i < 2; i++) {
                            heroes[i].addMana(getCurrentTurn());
                            heroes[i].drawCard();
                        }
                        for (int i = 0; i < tableHeight; i++) {
                            for (int j = 0; j < tableWidth; j++) {
                                if (playedCards[i][j] == null)
                                    break;
                                playedCards[i][j].setFrozen(false);
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void onMinionDeath(Minion minion) {
        for (int i = minion.getX(); i < tableWidth - 1; i++) {
            playedCards[minion.getY()][i] = playedCards[minion.getY()][i + 1];
        }
        playedCards[minion.getY()][tableWidth - 1] = null;
    }

    public void onHeroDeath(Hero hero) {
        isGameOver = true;
    }

    public void onEntityDeath(Entity entity) {
        if (entity.card.type == CardType.HERO) {
            onHeroDeath((Hero) entity);
            return;
        }
        onMinionDeath((Minion) entity);
    }
}
