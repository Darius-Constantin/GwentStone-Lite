package org.poo.main;

import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.IOHandler;
import org.poo.meta_game.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     *
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException, IllegalAccessException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            //if (!file.getName().equalsIgnoreCase("test01_game_start.json"))
            //    continue;
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException, IllegalAccessException {
        IOHandler.INSTANCE.handleInput(CheckerConstants.TESTS_PATH + filePath1);
        Session session = new Session(IOHandler.INSTANCE.getInputData().getPlayerOneDecks(),
                IOHandler.INSTANCE.getInputData().getPlayerTwoDecks(),
                IOHandler.INSTANCE.getInputData().getGames());
        session.beginSession();
        IOHandler.INSTANCE.handleOutput(filePath2);
    }
}
