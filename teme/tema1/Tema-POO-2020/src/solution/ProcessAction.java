package solution;

import actor.ActorsAwards;
import common.Constants;
import entertainment.Season;
import fileio.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.Utils;

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
                                    object.put(Constants.MESSAGE, "success -> " + action.getTitle() + " was added as favourite");
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
                                            object.put(Constants.MESSAGE, "error -> " + action.getTitle() + " has been already rated");
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
                                            object.put(Constants.MESSAGE, "error -> " + action.getTitle() + " has been already rated");
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
                            object.put(Constants.MESSAGE, "error -> " + action.getTitle() + " is not seen");
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
        JSONObject object = new JSONObject();
        object.put(Constants.ID_STRING, action.getActionId());
        StringBuilder message = new StringBuilder("Query result: [");
        int cnt;
        boolean ok;
        String stringAux;

        switch (action.getObjectType()) {
            case "actors" -> {
                ArrayList<String> copyActors = new ArrayList<>();
                boolean auxiliaryOk;

                switch (action.getCriteria()) {
                    case "average" -> {
                        double result, seasonResult, doubleAux;
                        ArrayList<Double> movieRatings = new ArrayList<>();
                        ArrayList<Double> serialRatings = new ArrayList<>();
                        ArrayList<Double> actorRatings = new ArrayList<>();

                        for (MovieInputData movie : input.getMovies()) {
                            result = 0.0;
                            cnt = 0;
                            for (Double rating : movie.getRatings()) {
                                if (!(rating.equals(0.0))) {
                                    result += rating;
                                    cnt++;
                                }
                            }
                            if (cnt != 0) {
                                result /= cnt;
                            }
                            movieRatings.add(result);
                        }

                        for (SerialInputData serial : input.getSerials()) {
                            result = 0.0;
                            for (Season season : serial.getSeasons()) {
                                seasonResult = 0.0;
                                cnt = 0;
                                for (Double rating : season.getRatings()) {
                                    if (!(rating.equals(0.0))) {
                                        seasonResult += rating;
                                        cnt++;
                                    }
                                }
                                if (cnt != 0) {
                                    seasonResult /= cnt;
                                }
                                result += seasonResult;
                            }
                            result /= serial.getNumberSeason();
                            serialRatings.add(result);
                        }

                        for (ActorInputData actor : input.getActors()) {
                            result = 0.0;
                            cnt = 0;
                            for (String title : actor.getFilmography()) {
                                for (MovieInputData movie : input.getMovies()) {
                                    if (movie.getTitle().equals(title)) {
                                        if (!(movieRatings.get(input.getMovies().indexOf(movie)).equals(0.0))) {
                                            result += movieRatings.get(input.getMovies().indexOf(movie));
                                            cnt++;
                                            break;
                                        }
                                    }
                                }
                                for (SerialInputData serial : input.getSerials()) {
                                    if (serial.getTitle().equals(title)) {
                                        if (!(serialRatings.get(input.getSerials().indexOf(serial)).equals(0.0))) {
                                            result += serialRatings.get(input.getSerials().indexOf(serial));
                                            cnt++;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (cnt != 0) {
                                result /= cnt;
                                actorRatings.add(result);
                                copyActors.add(actor.getName());
                            }
                        }

                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {
                                ok = false;
                                if ((Double.compare(actorRatings.get(i), actorRatings.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(actorRatings.get(i), actorRatings.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(actorRatings.get(i), actorRatings.get(j)) == 0)) {
                                    if ((copyActors.get(i).compareTo(copyActors.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyActors.get(i).compareTo(copyActors.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    doubleAux = actorRatings.get(i);
                                    actorRatings.set(i, actorRatings.get(j));
                                    actorRatings.set(j, doubleAux);

                                    stringAux = copyActors.get(i);
                                    copyActors.set(i, copyActors.get(j));
                                    copyActors.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "awards" -> {
                        int result, intAux;
                        ArrayList<Integer> awardsCount = new ArrayList<>();

                        for (ActorInputData actor : input.getActors()) {
                            result = 0;
                            auxiliaryOk = true;

                            for (Map.Entry<ActorsAwards, Integer> element : actor.getAwards().entrySet()) {
                                result += element.getValue();
                            }

                            for (String searchedAward : action.getFilters().get(3)) {
                                ok = false;
                                for (Map.Entry<ActorsAwards, Integer> element : actor.getAwards().entrySet()) {
                                    if (element.getKey().equals(Utils.stringToAwards(searchedAward))) {
                                        ok = true;
                                        break;
                                    }
                                }
                                if (!ok) {
                                    auxiliaryOk = false;
                                    break;
                                }
                            }
                            if (auxiliaryOk) {
                                awardsCount.add(result);
                                copyActors.add(actor.getName());
                            }
                        }

                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {
                                ok = false;
                                if (awardsCount.get(i) < awardsCount.get(j) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if (awardsCount.get(i) > awardsCount.get(j) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (awardsCount.get(i).equals(awardsCount.get(j))) {
                                    if ((copyActors.get(i).compareTo(copyActors.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyActors.get(i).compareTo(copyActors.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = awardsCount.get(i);
                                    awardsCount.set(i, awardsCount.get(j));
                                    awardsCount.set(j, intAux);

                                    stringAux = copyActors.get(i);
                                    copyActors.set(i, copyActors.get(j));
                                    copyActors.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "filter_description" -> {
                        String description;

                        for (ActorInputData actor : input.getActors()) {
                            description = actor.getCareerDescription().toLowerCase();
                            auxiliaryOk = true;
                            for (String searchedWord : action.getFilters().get(2)) {
                                ok = false;
                                for (String word : description.split("[-.?! +/\n]+")) {
                                    if (searchedWord.toLowerCase().equals(word)) {
                                        ok = true;
                                        break;
                                    }
                                }
                                if (!ok) {
                                    auxiliaryOk = false;
                                    break;
                                }
                            }
                            if (auxiliaryOk) {
                                copyActors.add(actor.getName());
                            }
                        }

                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {
                                ok = false;
                                if ((copyActors.get(i).compareTo(copyActors.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((copyActors.get(i).compareTo(copyActors.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }

                                if (ok) {
                                    stringAux = copyActors.get(i);
                                    copyActors.set(i, copyActors.get(j));
                                    copyActors.set(j, stringAux);
                                }
                            }
                        }
                    }
                    default -> System.out.println("Incorrect query");
                }
                cnt = action.getNumber();
                if (cnt > copyActors.size()) {
                    cnt = copyActors.size();
                }
                for (int i = 0; i < cnt - 1; i++) {
                    message.append(copyActors.get(i));
                    message.append(", ");
                }
                if (cnt > 0) {
                    message.append(copyActors.get(cnt - 1));
                }
            }
            case "movies" -> {
                ArrayList<String> copyMovies = new ArrayList<>();
                String year = action.getFilters().get(0).get(0);
                String genre = action.getFilters().get(1).get(0);

                switch (action.getCriteria()) {
                    case "ratings" -> {
                        double result, doubleAux;
                        ArrayList<Double> movieRatings = new ArrayList<>();

                        for (MovieInputData movie : input.getMovies()) {
                            result = 0.0;
                            cnt = 0;
                            for (Double rating : movie.getRatings()) {
                                if (!(rating.equals(0.0))) {
                                    result += rating;
                                    cnt++;
                                }
                            }

                            if (((year == null) || (movie.getYear() == Integer.parseInt(year))) && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    result /= cnt;
                                    movieRatings.add(result);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {
                                ok = false;
                                if ((Double.compare(movieRatings.get(i), movieRatings.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(movieRatings.get(i), movieRatings.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(movieRatings.get(i), movieRatings.get(j)) == 0)) {
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    doubleAux = movieRatings.get(i);
                                    movieRatings.set(i, movieRatings.get(j));
                                    movieRatings.set(j, doubleAux);

                                    stringAux = copyMovies.get(i);
                                    copyMovies.set(i, copyMovies.get(j));
                                    copyMovies.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "favorite" -> {
                        int  intAux;
                        ArrayList<Integer> movieCount = new ArrayList<>();

                        for (MovieInputData movie : input.getMovies()) {
                            cnt = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getFavoriteMovies().contains(movie.getTitle())) {
                                    cnt++;
                                }
                            }

                            if (((year == null) || (movie.getYear() == Integer.parseInt(year))) && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    movieCount.add(cnt);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {
                                ok = false;
                                if ((movieCount.get(i) < movieCount.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((movieCount.get(i) > movieCount.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (movieCount.get(i).equals(movieCount.get(j))) {
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = movieCount.get(i);
                                    movieCount.set(i, movieCount.get(j));
                                    movieCount.set(j, intAux);

                                    stringAux = copyMovies.get(i);
                                    copyMovies.set(i, copyMovies.get(j));
                                    copyMovies.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "longest" -> {
                        int  intAux;
                        ArrayList<Integer> movieDuration = new ArrayList<>();

                        for (MovieInputData movie : input.getMovies()) {
                            if (((year == null) || (movie.getYear() == Integer.parseInt(year))) && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                movieDuration.add(movie.getDuration());
                                copyMovies.add(movie.getTitle());
                            }
                        }

                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {
                                ok = false;
                                if ((movieDuration.get(i) < movieDuration.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((movieDuration.get(i) > movieDuration.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (movieDuration.get(i).equals(movieDuration.get(j))) {
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = movieDuration.get(i);
                                    movieDuration.set(i, movieDuration.get(j));
                                    movieDuration.set(j, intAux);

                                    stringAux = copyMovies.get(i);
                                    copyMovies.set(i, copyMovies.get(j));
                                    copyMovies.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "most_viewed" -> {
                        int  result, intAux;
                        ArrayList<Integer> movieCount = new ArrayList<>();

                        for (MovieInputData movie : input.getMovies()) {
                            result = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getHistory().containsKey(movie.getTitle())) {
                                    result += user.getHistory().get(movie.getTitle());
                                }
                            }

                            if (((year == null) || (movie.getYear() == Integer.parseInt(year))) && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    movieCount.add(result);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {
                                ok = false;
                                if ((movieCount.get(i) < movieCount.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((movieCount.get(i) > movieCount.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (movieCount.get(i).equals(movieCount.get(j))) {
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = movieCount.get(i);
                                    movieCount.set(i, movieCount.get(j));
                                    movieCount.set(j, intAux);

                                    stringAux = copyMovies.get(i);
                                    copyMovies.set(i, copyMovies.get(j));
                                    copyMovies.set(j, stringAux);
                                }
                            }
                        }
                    }
                    default -> System.out.println("Incorrect query");
                }
                cnt = action.getNumber();
                if (cnt > copyMovies.size()) {
                    cnt = copyMovies.size();
                }
                for (int i = 0; i < cnt - 1; i++) {
                    message.append(copyMovies.get(i));
                    message.append(", ");
                }
                if (cnt > 0) {
                    message.append(copyMovies.get(cnt - 1));
                }
            }
            case "shows" -> {
                ArrayList<String> copyShows = new ArrayList<>();
                String year = action.getFilters().get(0).get(0);
                String genre = action.getFilters().get(1).get(0);

                switch (action.getCriteria()) {
                    case "ratings" -> {
                        double result, seasonResult, doubleAux;
                        ArrayList<Double> serialRatings = new ArrayList<>();

                        for (SerialInputData serial : input.getSerials()) {
                            result = 0.0;
                            for (Season season : serial.getSeasons()) {
                                seasonResult = 0.0;
                                cnt = 0;
                                for (Double rating : season.getRatings()) {
                                    if (!(rating.equals(0.0))) {
                                        seasonResult += rating;
                                        cnt++;
                                    }
                                }
                                if (cnt != 0) {
                                    seasonResult /= cnt;
                                }
                                result += seasonResult;
                            }

                            if (((year == null) || (serial.getYear() == Integer.parseInt(year))) && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    result /= serial.getNumberSeason();
                                    serialRatings.add(result);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {
                                ok = false;
                                if ((Double.compare(serialRatings.get(i), serialRatings.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(serialRatings.get(i), serialRatings.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if ((Double.compare(serialRatings.get(i), serialRatings.get(j)) == 0)) {
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    doubleAux = serialRatings.get(i);
                                    serialRatings.set(i, serialRatings.get(j));
                                    serialRatings.set(j, doubleAux);

                                    stringAux = copyShows.get(i);
                                    copyShows.set(i, copyShows.get(j));
                                    copyShows.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "favorite" -> {
                        int  intAux;
                        ArrayList<Integer> serialCount = new ArrayList<>();

                        for (SerialInputData serial : input.getSerials()) {
                            cnt = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getFavoriteMovies().contains(serial.getTitle())) {
                                    cnt++;
                                }
                            }

                            if (((year == null) || (serial.getYear() == Integer.parseInt(year))) && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    serialCount.add(cnt);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {
                                ok = false;
                                if ((serialCount.get(i) < serialCount.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((serialCount.get(i) > serialCount.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (serialCount.get(i).equals(serialCount.get(j))) {
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = serialCount.get(i);
                                    serialCount.set(i, serialCount.get(j));
                                    serialCount.set(j, intAux);

                                    stringAux = copyShows.get(i);
                                    copyShows.set(i, copyShows.get(j));
                                    copyShows.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "longest" -> {
                        int  intAux, totalDuration = 0;
                        ArrayList<Integer> serialDuration = new ArrayList<>();

                        for (SerialInputData serial : input.getSerials()) {
                            if (((year == null) || (serial.getYear() == Integer.parseInt(year))) && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                for (int i = 0; i < serial.getNumberSeason(); i++) {
                                    totalDuration += serial.getSeasons().get(i).getDuration();
                                }
                                serialDuration.add(totalDuration);
                                copyShows.add(serial.getTitle());
                            }
                        }

                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {
                                ok = false;
                                if ((serialDuration.get(i) < serialDuration.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((serialDuration.get(i) > serialDuration.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (serialDuration.get(i).equals(serialDuration.get(j))) {
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = serialDuration.get(i);
                                    serialDuration.set(i, serialDuration.get(j));
                                    serialDuration.set(j, intAux);

                                    stringAux = copyShows.get(i);
                                    copyShows.set(i, copyShows.get(j));
                                    copyShows.set(j, stringAux);
                                }
                            }
                        }
                    }
                    case "most_viewed" -> {
                        int  result, intAux;
                        ArrayList<Integer> serialCount = new ArrayList<>();

                        for (SerialInputData serial : input.getSerials()) {
                            result = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getHistory().containsKey(serial.getTitle())) {
                                    result += user.getHistory().get(serial.getTitle());
                                }
                            }

                            if (((year == null) || (serial.getYear() == Integer.parseInt(year))) && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    serialCount.add(result);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {
                                ok = false;
                                if ((serialCount.get(i) < serialCount.get(j)) && (action.getSortType().equals("desc"))) {
                                    ok = true;
                                }
                                if ((serialCount.get(i) > serialCount.get(j)) && (action.getSortType().equals("asc"))) {
                                    ok = true;
                                }
                                if (serialCount.get(i).equals(serialCount.get(j))) {
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                        ok = true;
                                    }
                                    if ((copyShows.get(i).compareTo(copyShows.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                        ok = true;
                                    }
                                }

                                if (ok) {
                                    intAux = serialCount.get(i);
                                    serialCount.set(i, serialCount.get(j));
                                    serialCount.set(j, intAux);

                                    stringAux = copyShows.get(i);
                                    copyShows.set(i, copyShows.get(j));
                                    copyShows.set(j, stringAux);
                                }
                            }
                        }
                    }
                    default -> System.out.println("Incorrect query");
                }
                cnt = action.getNumber();
                if (cnt > copyShows.size()) {
                    cnt = copyShows.size();
                }
                for (int i = 0; i < cnt - 1; i++) {
                    message.append(copyShows.get(i));
                    message.append(", ");
                }
                if (cnt > 0) {
                    message.append(copyShows.get(cnt - 1));
                }
            }
            case "users" -> {
                int intAux;
                ArrayList<String> copyUsers = new ArrayList<>();
                ArrayList<Integer> userRatings = new ArrayList<>();

                for (UserInputData user : input.getUsers()) {
                    cnt = 0;
                    for (MovieInputData movie : input.getMovies()) {
                        if (!(movie.getRatings().get(input.getUsers().indexOf(user)).equals(0.0))) {
                            cnt++;
                        }
                    }

                    for (SerialInputData serial : input.getSerials()) {
                        for (Season season : serial.getSeasons()) {
                            if (!(season.getRatings().get(input.getUsers().indexOf(user)).equals(0.0))) {
                                cnt++;
                            }
                        }
                    }

                    if (cnt != 0) {
                        userRatings.add(cnt);
                        copyUsers.add(user.getUsername());
                    }
                }

                for (int i = 0; i < copyUsers.size() - 1; i++) {
                    for (int j = i + 1; j < copyUsers.size(); j++) {
                        ok = false;
                        if ((userRatings.get(i) < userRatings.get(j)) && (action.getSortType().equals("desc"))) {
                            ok = true;
                        }
                        if ((userRatings.get(i) > userRatings.get(j)) && (action.getSortType().equals("asc"))) {
                            ok = true;
                        }
                        if (userRatings.get(i).equals(userRatings.get(j))) {
                            if ((copyUsers.get(i).compareTo(copyUsers.get(j)) < 0) && (action.getSortType().equals("desc"))) {
                                ok = true;
                            }
                            if ((copyUsers.get(i).compareTo(copyUsers.get(j)) > 0) && (action.getSortType().equals("asc"))) {
                                ok = true;
                            }
                        }

                        if (ok) {
                            intAux = userRatings.get(i);
                            userRatings.set(i, userRatings.get(j));
                            userRatings.set(j, intAux);

                            stringAux = copyUsers.get(i);
                            copyUsers.set(i, copyUsers.get(j));
                            copyUsers.set(j, stringAux);
                        }
                    }
                }

                cnt = action.getNumber();
                if (cnt > copyUsers.size()) {
                    cnt = copyUsers.size();
                }
                for (int i = 0; i < cnt - 1; i++) {
                    message.append(copyUsers.get(i));
                    message.append(", ");
                }
                if (cnt > 0) {
                    message.append(copyUsers.get(cnt - 1));
                }
            }
            default -> System.out.println("Incorrect query");
        }
        message.append("]");
        object.put(Constants.MESSAGE, message.toString());
        arrayResult.add(object);
    }

    /**
     * Does all operations and changes needed for a
     * "recommendation" type command
     * @param action the action that needs to be done
     */
    public void processRecommendation(ActionInputData action) {
        switch (action.getType()) {
            case "standard" -> {
                System.out.println("standard");
            }
            case "best_unseen" -> {
                System.out.println("best_unseen");
            }
            case "popular" -> {
                System.out.println("popular");
            }
            case "favorite" -> {
                System.out.println("favorite");
            }case "search" -> {
                System.out.println("search");
            }
            default -> System.out.println("Incorrect recommendation");


        }
    }

}
