# Gwentstone Lite

## Requirements and usage

Java SDK 21 is required to run the application from Main. As input, JSON files are required. For examples of file structure, inspect [the input folder](input).

## Implementation
### Structure

The structure of the implementation is as follows (only notable characteristics):
* Session
    * Player
        * Decks
        * Number of wins
    * IOHandler
        * Input and output handling
    * Games
        * Heroes [2]
        * Played cards (table)
            * Minions


* Entity
    * Card, all entities will base their attributes on a given card. No Entity can exist without a card. This design choice was taken to avoid wasting memory on duplicate memory/pointers, when a singular reference to the card does the job well.
    * Death dispatcher
    * A boolean that keeps track of whether the entity has acted during a turn


* Hero (Entity)
    * HeroCard that describes the properties of the instanced Hero
    * Health
    * Deck cards
    * In-hand cards
    * Mana


* Minion
    * Coordinates
    * A boolean that keeps track of whether the minion is frozen
    * Attack damage


* Game
    * Constants to describe the size of the playing field/table, ownership of rows
    * Runtime statistics
    * COMMANDS_MAP, a HashMap between String commands and a Command enum which stores the functionality of each command
    * MINION_CONSTRUCTOR, a HashMap between the name of a minion and its specific constructor. This is particularly useful to avoid gigantic switches for placing cards/spawning minions.

### Patterns used
Throughout the assignment, three overarching patterns have been used in three different contexts:
* **Singleton pattern**: In order to handle input in output, the singleton class `IOHandler` manages them alone. This is among the cases where singletons are preferable to avoid multiple instancing.


* **Event-driven architecture**: I am not aware of a specific name for this pattern, however its working is simple: Whenever an event takes place, instead of modifying the implementation of the event to notify other instances of its occurrence, instances can "subscribe" or "register" a function (a specific reaction to the event) that will be called by a so-named dispatcher which is the only object directly notified by the happening of the event. Thus, as long as every significant event has an associated dispatcher, other classes can be let known about it through the dispatcher, no matter if, in time, more class will wish to be notified by the event. This seems to be a very nice pattern for applications such as this one, and I wish I would've thought about and used it sooner during development.


* **Command pattern**: Given the text-based nature of the game, this pattern consists of parsing commands, finding the associated function of the command in a HashMap (fast retrieval), which is then simply called.

### Flow

Once an input JSON file is parsed by the `IOHandler`, a new `Session` is created which is responsible for tracking however many battles two players may choose to engage in. Once a Session beings playing, it will iterate over all input games and, for each, instance a `Game` which mainly consists of the players' chosen heroes. Once a Game is constructed, it can commence playing, which will in turn iterate over each input actions. Actions will be taken and be logged on fail (if rules are being broken) and on success (with its effect). Once a hero dies, all remaining actions are taken (unless they are `GAMEPLAY` actions which would modify the state of the game), then the Game ends, and following Games begin being played.

### Difficulties during the implementation

I had initially implemented a much smarter system, where there were no specific implementations of minions based on their name, where spawning a minion required only a Card and its attributes, but due to the restrictions of the assignment, [third row from below](https://ocw.cs.pub.ro/courses/poo-ca-cd/administrativ/barem_teme), I needed to scrap that idea to comply.

I also needed to scrap a hand-made wrapper over Jackson which used reflexivity (and inherently `instanceof` and `getClass()`) which I chose to write to try my hand at custom annotations, so I had to rework the `IOHandler` class, but it is what it is.

## Possible improvements

Currently, after creating a minion/hero, it must also be added inside [MinionEnum](src/main/java/org/poo/game/MinionEnum.java)/[HeroEnum](src/main/java/org/poo/game/HeroEnum.java) to make it available, which means extra work besides just implementing the class, but without reflexivity I have no clue yet about how to automate the process.

### Author

**Darius Constantin**

* [github/Darius-Constantin](https://github.com/Darius-Constantin)

### License

Copyright © 2024, [Darius Constantin](https://github.com/Darius-Constantin).
Released under the [MIT License](https://opensource.org/license/mit).****