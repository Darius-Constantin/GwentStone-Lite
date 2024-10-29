package game;

import cards.Card;
import cards.HeroCard;
import cards.MinionCard;
import fileio.CardInput;
import fileio.GameInput;
import fileio.DecksInput;
import fileio.IOHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Session {
    private final Player[] players = new Player[2];
    private Game game;
    @Setter
    @Getter
    private int noWinsPlayer1 = 0;
    @Setter
    @Getter
    private int noWinsPlayer2 = 0;
    private final ArrayList<GameInput> games;

    public Session(DecksInput inputPlayer1Decks, DecksInput inputPlayer2Decks, ArrayList<GameInput> games) {
        ArrayList<ArrayList<MinionCard>> player1Decks = new ArrayList<ArrayList<MinionCard>>();
        for (ArrayList<CardInput> deck : inputPlayer1Decks.getDecks()) {
            player1Decks.add(new ArrayList<MinionCard>());
            for (CardInput cardInput : deck)
                player1Decks.get(player1Decks.size() - 1).add(new MinionCard(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
        }
        players[0] = new Player(inputPlayer1Decks.getNrDecks(), inputPlayer1Decks.getNrCardsInDeck(), player1Decks);

        ArrayList<ArrayList<MinionCard>> player2Decks = new ArrayList<ArrayList<MinionCard>>();
        for (ArrayList<CardInput> deck : inputPlayer2Decks.getDecks()) {
            player2Decks.add(new ArrayList<MinionCard>());
            for (CardInput cardInput : deck)
                player2Decks.get(player2Decks.size() - 1).add(new MinionCard(cardInput.getHealth(),
                        cardInput.getMana(),
                        cardInput.getAttackDamage(),
                        cardInput.getDescription(),
                        cardInput.getColors(),
                        cardInput.getName()));
        }
        players[1] = new Player(inputPlayer1Decks.getNrDecks(), inputPlayer1Decks.getNrCardsInDeck(), player2Decks);
        this.games = games;
    }

    public void beginSession() {
        for (GameInput gameInput : games) {
            CardInput tmpHero = gameInput.getStartGame().getPlayerOneHero();
            Card player1HeroCard = new HeroCard(tmpHero.getMana(), tmpHero.getAttackDamage(), tmpHero.getDescription(),
                    tmpHero.getColors(), tmpHero.getName());

            tmpHero = gameInput.getStartGame().getPlayerTwoHero();
            Card player2HeroCard = new HeroCard(tmpHero.getMana(), tmpHero.getAttackDamage(), tmpHero.getDescription(),
                    tmpHero.getColors(), tmpHero.getName());

            Game game = new Game(this, gameInput.getStartGame().getShuffleSeed(),
                    gameInput.getStartGame().getStartingPlayer() - 1,
                    players[0].getDecks().get(gameInput.getStartGame().getPlayerOneDeckIdx()),
                    players[1].getDecks().get(gameInput.getStartGame().getPlayerTwoDeckIdx()),
                    player1HeroCard, player2HeroCard, gameInput.getActions());

            game.playGame();
        }
    }
}
