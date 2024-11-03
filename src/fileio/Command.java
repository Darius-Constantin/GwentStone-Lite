package fileio;

import lombok.Getter;

@Getter
@SuppressWarnings("SpellCheckingInspection")
public enum Command {
    PLACECARD("placeCard", CommandType.GAMEPLAY),
    CARDUSESATTACK("cardUsesAttack", CommandType.GAMEPLAY),
    CARDUSESABILITY("cardUsesAbility", CommandType.GAMEPLAY),
    USEATTACKHERO("useAttackHero", CommandType.GAMEPLAY),
    USEHEROABILITY("useHeroAbility", CommandType.GAMEPLAY),
    ENDPLAYERTURN("endPlayerTurn", CommandType.GAMEPLAY),
    GETPLAYERDECK("getPlayerDeck", CommandType.DEBUG),
    GETPLAYERHERO("getPlayerHero", CommandType.DEBUG),
    GETPLAYERTURN("getPlayerTurn", CommandType.DEBUG),
    GETFROZENCARDSONTABLE("getFrozenCardsOnTable", CommandType.DEBUG),
    GETPLAYERMANA("getPlayerMana", CommandType.DEBUG),
    GETCARDATPOSITION("getCardAtPosition", CommandType.DEBUG),
    GETCARDSONTABLE("getCardsOnTable", CommandType.DEBUG),
    GETCARDSINHAND("getCardsInHand", CommandType.DEBUG),
    GETTOTALGAMESPLAYED("getTotalGamesPlayed", CommandType.STATS),
    GETPLAYERONEWINS("getPlayerOneWins", CommandType.STATS),
    GETPLAYERTWOWINS("getPlayerTwoWins", CommandType.STATS);

    private final String command;
    private final CommandType commandType;
    private Command(final String command, final CommandType type) {
        this.command = command;
        this.commandType = type;
    }

    public static Command getCommandFromString(final String command) {
        for (var type : Command.values()) {
            if (type.command.equals(command)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
}
