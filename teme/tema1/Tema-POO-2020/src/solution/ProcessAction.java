package solution;

import common.Constants;
import entertainment.Season;
import fileio.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessAction {
    /**
     * A copy of the whole database,  from main's Input input
     */
    private final Input input;
    /**
     * The array of JSON objects with all the id + message pairs
     */
    private final JSONArray arrayResult;

    public ProcessAction(Input input, JSONArray arrayResult) {
        this.input = input;
        this.arrayResult = arrayResult;
    }

    /**
     * Does all operations and changes needed for a
     * "action" type command
     * @param action the action that needs to be done
     */
    public void processCommand(ActionInputData action) {
        JSONObject object = new JSONObject();
        object.put(Constants.ID_STRING, action.getActionId());
        boolean ok = true;
        boolean auxiliaryOk = true;

        switch (action.getType()) {
            case "favorite" -> {
                for (UserInputData user : input.getUsers()) {
                    if (user.getUsername().equals(action.getUsername())) {
                        for (Map.Entry<String, Integer> element : user.getHistory().entrySet()) {
                            if (element.getKey().equals(action.getTitle())) {
                                auxiliaryOk = false;
                                for (String favourite : user.getFavoriteMovies()) {
                                    if (favourite.equals(action.getTitle())) {
                                        object.put(Constants.MESSAGE, "error -> " + action.getTitle() + " is already in favourite list");
                                        ok = false;
                                        break;
                                    }
                                }
                                if (ok) {
                                    user.getFavoriteMovies().add(action.getTitle());
                                    object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " added to the favourite list");
                                }
                            }
                        }
                        if (auxiliaryOk) {
                            object.put(Constants.MESSAGE, "error -> " + action.getTitle() + " is not seen");
                        }
                    }
                }
            }
            case "view" -> {
                for (UserInputData user : input.getUsers()) {
                    if (user.getUsername().equals(action.getUsername())) {
                        for (Map.Entry<String, Integer> element : user.getHistory().entrySet()) {
                            if (element.getKey().equals(action.getTitle())) {
                                user.getHistory().put(action.getTitle(), element.getValue() + 1);
                                object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " was viewed with total views of " + element.getValue());
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            user.getHistory().put(action.getTitle(), 1);
                            object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " was viewed with total views of 1");
                        }
                    }
                }
            }
            case "rating" -> {
                int cnt = -1;
                for (UserInputData user : input.getUsers()) {
                    cnt++;
                    if (user.getUsername().equals(action.getUsername())) {
                        for (Map.Entry<String, Integer> element : user.getHistory().entrySet()) {
                            if (element.getKey().equals(action.getTitle())) {
                                for (MovieInputData movie : input.getMovies()) {
                                    if (action.getTitle().equals(movie.getTitle())) {
                                        if (!(movie.getRatings().get(cnt).equals(0.0))) {
                                            object.put(Constants.MESSAGE, "error-> " + action.getTitle() + " has been already rated");
                                        } else {
                                            ArrayList<Double> tempList = new ArrayList<>(movie.getRatings());
                                            tempList.set(cnt, action.getGrade());
                                            movie.setRatings(tempList);
                                            object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " was rated with " + action.getGrade() + " by " + action.getUsername());
                                        }
                                        ok = false;
                                        break;
                                    }
                                }
                                for (SerialInputData serial : input.getSerials()) {
                                    if (action.getTitle().equals(serial.getTitle())) {
                                        Season season = serial.getSeasons().get(action.getSeasonNumber() - 1);
                                        if (!(season.getRatings().get(cnt).equals(0.0))) {
                                            object.put(Constants.MESSAGE, "error-> " + action.getTitle() + " has been already rated");
                                        } else {
                                            ArrayList<Double> tempList = new ArrayList<>(season.getRatings());
                                            tempList.set(cnt, action.getGrade());
                                            season.setRatings(tempList);
                                            object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " was rated with " + action.getGrade() + " by " + action.getUsername());
                                        }
                                        ok = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (ok) {
                            object.put(Constants.MESSAGE, "error-> " + action.getTitle() + " is not seen");
                        }
                    }
                }
            }
            default -> System.out.println("Incorrect action");
        }
        arrayResult.add(object);
    }

    /**
     * Does all operations and changes needed for a
     * "query" type command
     * @param action the action that needs to be done
     */
    public void processQuery(ActionInputData action) {
        switch (action.getObjectType()) {
            case "actors" -> {
                switch (action.getCriteria()) {
                    case "average" -> {
                        Double result = 0.0;
                        int cnt = 0;
                        for (MovieInputData movie : input.getMovies()) {
                            for (Double rating : movie.getRatings()) {
                                result += rating;
                            }
                        }
                    }
                    case "awards" -> {
                        System.out.println("awards");
                    }
                    case "filter_description" -> {
                        System.out.println("filter_description");
                    }
                }
            }
            case "movies" -> {
                System.out.println("movies");
            }
            case "users" -> {
                System.out.println("users");
            }
            default -> System.out.println("Incorrect query");
        }

    }

    /**
     * Does all operations and changes needed for a
     * "recommendation" type command
     * @param action the action that needs to be done
     */
    public void processRecommendation(ActionInputData action) {

    }

}
