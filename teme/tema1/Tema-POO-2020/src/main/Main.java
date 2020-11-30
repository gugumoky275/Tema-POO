package main;

import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import fileio.Input;
import fileio.InputLoader;
import fileio.Writer;
import fileio.MovieInputData;
import fileio.ActionInputData;
import fileio.SerialInputData;
import entertainment.Season;
import solution.ProcessAction;

import org.json.simple.JSONArray;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * Call the main checker and the coding style checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(Constants.TESTS_PATH);
        Path path = Paths.get(Constants.RESULT_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        File outputDirectory = new File(Constants.RESULT_PATH);

        Checker checker = new Checker();
        checker.deleteFiles(outputDirectory.listFiles());

        for (File file : Objects.requireNonNull(directory.listFiles())) {

            String filepath = Constants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getAbsolutePath(), filepath);
            }
        }

        checker.iterateFiles(Constants.RESULT_PATH, Constants.REF_PATH, Constants.TESTS_PATH);
        Checkstyle test = new Checkstyle();
        test.testCheckstyle();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        InputLoader inputLoader = new InputLoader(filePath1);
        Input input = inputLoader.readData();

        Writer fileWriter = new Writer(filePath2);
        JSONArray arrayResult = new JSONArray();

        // The beginning of my implementation

        // Initialization of all show ratings
        List<Double> showRatings = new ArrayList<>();
        for (int i = 0; i < input.getUsers().size(); i++) {
            showRatings.add(0.0);
        }

        for (MovieInputData movie : input.getMovies()) {
            movie.setRatings(showRatings);
        }
        for (SerialInputData serial : input.getSerials()) {
            for (Season season : serial.getSeasons()) {
                season.setRatings(showRatings);
            }
        }

        // Processing each action at a time, in class ProcessAction
        ProcessAction processAction = new ProcessAction(input, arrayResult);
        for (ActionInputData action : input.getCommands()) {
            switch (action.getActionType()) {
                case "command" -> processAction.processCommand(action);
                case "query" -> processAction.processQuery(action);
                case "recommendation" -> processAction.processRecommendation(action);
                default -> System.out.println("Incorrect action");
            }
        }
        // The end of my implementation

        fileWriter.closeJSON(arrayResult);
    }
}
