package org.poo.game.cards;

import lombok.Getter;

@Getter
public enum CardType {
    GENERIC(CardType.NO_CHECK),
    TAUNT((byte) (CardType.TAUNTER | CardType.FRONTLINER)),
    SPECIAL_FRONTLINE((byte) (CardType.FRONTLINER | CardType.SPECIAL)),
    SPECIAL_BACKLINE((byte) (CardType.BACKLINER | CardType.SPECIAL)),
    HERO(CardType.NO_CHECK);

    public static final byte NO_CHECK = (byte) ~0;
    public static final byte BACKLINER = 0b00000000;
    public static final byte FRONTLINER = 0b00000001;
    public static final byte TAUNTER = 0b00000010;
    public static final byte SPECIAL = 0b00000100;
    private final byte attributes;

    CardType(final byte attributes) {
        this.attributes = attributes;
    }
}
