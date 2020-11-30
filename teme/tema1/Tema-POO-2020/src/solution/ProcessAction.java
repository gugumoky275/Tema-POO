package solution;

import actor.ActorsAwards;
import common.Constants;
import entertainment.Genre;
import entertainment.Season;

import fileio.SerialInputData;
import fileio.MovieInputData;
import fileio.UserInputData;
import fileio.ActorInputData;
import fileio.Input;
import fileio.ActionInputData;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.Utils;

import java.util.ArrayList;
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

    // Copy the database and the JSONArray where the answer will be put
    public ProcessAction(final Input input, final JSONArray arrayResult) {
        this.input = input;
        this.arrayResult = arrayResult;
    }

    /**
     * Does all operations and changes needed for a
     * "action" type command
     * @param action the action that needs to be done
     */
    @SuppressWarnings("unchecked")
    public void processCommand(final ActionInputData action) {
        JSONObject object = new JSONObject();
        object.put(Constants.ID_STRING, action.getActionId());

        switch (action.getType()) {
            case "favorite" -> {
                for (UserInputData user : input.getUsers()) {

                    // Find user in database and check history
                    if (user.getUsername().equals(action.getUsername())) {
                        if (user.getHistory().containsKey(action.getTitle())) {
                            if (user.getFavoriteMovies().contains(action.getTitle())) {
                                object.put(Constants.MESSAGE, "error -> "
                                        + action.getTitle()
                                        + " is already in favourite list");

                            } else {
                                user.getFavoriteMovies().add(action.getTitle());
                                object.put(Constants.MESSAGE, "success -> "
                                        + action.getTitle()
                                        + " was added as favourite");
                            }
                        } else {
                            object.put(Constants.MESSAGE, "error -> "
                                    + action.getTitle()
                                    + " is not seen");
                        }
                        break;
                    }
                }
            }
            case "view" -> {
                for (UserInputData user : input.getUsers()) {

                    // Find user in database and check if seen
                    if (user.getUsername().equals(action.getUsername())) {
                        if (user.getHistory().containsKey(action.getTitle())) {
                            user.getHistory().put(action.getTitle(),
                                    user.getHistory().get(action.getTitle()) + 1);
                            object.put(Constants.MESSAGE, "success -> "
                                    + action.getTitle() + " was viewed with total views of "
                                    + user.getHistory().get(action.getTitle()));

                        } else {
                            user.getHistory().put(action.getTitle(), 1);
                            object.put(Constants.MESSAGE, "success -> "
                                    + action.getTitle()
                                    + " was viewed with total views of 1");
                        }
                        break;
                    }
                }
            }
            case "rating" -> {
                int cnt = -1;
                boolean ok = true;

                for (UserInputData user : input.getUsers()) {
                    cnt++;

                    // Find user in database and check if seen video, or rated already
                    if (user.getUsername().equals(action.getUsername())) {
                        if (user.getHistory().containsKey(action.getTitle())) {

                            // Search the video through movies
                            for (MovieInputData movie : input.getMovies()) {
                                if (action.getTitle().equals(movie.getTitle())) {
                                    if (!(movie.getRatings().get(cnt).equals(0.0))) {
                                        object.put(Constants.MESSAGE, "error -> "
                                                + action.getTitle()
                                                + " has been already rated");
                                    } else {
                                        ArrayList<Double> tempList =
                                                new ArrayList<>(movie.getRatings());
                                        tempList.set(cnt, action.getGrade());
                                        movie.setRatings(tempList);
                                        object.put(Constants.MESSAGE, "success -> "
                                                + action.getTitle()
                                                + " was rated with "
                                                + action.getGrade()
                                                + " by " + action.getUsername());
                                    }
                                    ok = false;
                                    break;
                                }
                            }

                            // Search the video through serials
                            for (SerialInputData serial : input.getSerials()) {
                                if (action.getTitle().equals(serial.getTitle())) {
                                    Season season =
                                            serial.getSeasons().get(action.getSeasonNumber() - 1);

                                    if (!(season.getRatings().get(cnt).equals(0.0))) {
                                        object.put(Constants.MESSAGE, "error -> "
                                                + action.getTitle()
                                                + " has been already rated");
                                    } else {
                                        ArrayList<Double> tempList =
                                                new ArrayList<>(season.getRatings());
                                        tempList.set(cnt, action.getGrade());
                                        season.setRatings(tempList);
                                        object.put(Constants.MESSAGE, "success -> "
                                                + action.getTitle()
                                                + " was rated with "
                                                + action.getGrade()
                                                + " by "
                                                + action.getUsername());
                                    }
                                    ok = false;
                                    break;
                                }
                            }
                        }

                        // Case not found in history
                        if (ok) {
                            object.put(Constants.MESSAGE, "error -> "
                                    + action.getTitle()
                                    + " is not seen");
                        }
                        break;
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
    @SuppressWarnings("unchecked")
    public void processQuery(final ActionInputData action) {
        JSONObject object = new JSONObject();
        object.put(Constants.ID_STRING, action.getActionId());
        StringBuilder message = new StringBuilder("Query result: [");

        int cnt;
        boolean ok;
        String stringAux;

        switch (action.getObjectType()) {
            case "actors" -> {
                ArrayList<String> copyActors = new ArrayList<>();


                switch (action.getCriteria()) {
                    case "average" -> {
                        double result, seasonResult, doubleAux;
                        ArrayList<Double> movieRatings = new ArrayList<>();
                        ArrayList<Double> serialRatings = new ArrayList<>();
                        ArrayList<Double> actorRatings = new ArrayList<>();

                        // Create movie average rating from all user ratings
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

                        // Create serial average rating from all user ratings
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

                        // For each actor search the videos they played in
                        for (ActorInputData actor : input.getActors()) {
                            result = 0.0;
                            cnt = 0;

                            for (String title : actor.getFilmography()) {
                                for (MovieInputData movie : input.getMovies()) {
                                    if (movie.getTitle().equals(title)) {
                                        if (!(movieRatings.get(input.getMovies().indexOf(movie))
                                                .equals(0.0))) {
                                            result += movieRatings
                                                    .get(input.getMovies().indexOf(movie));
                                            cnt++;
                                            break;
                                        }
                                    }
                                }

                                for (SerialInputData serial : input.getSerials()) {
                                    if (serial.getTitle().equals(title)) {
                                        if (!(serialRatings.get(input.getSerials().indexOf(serial))
                                                .equals(0.0))) {
                                            result += serialRatings.get(input.getSerials()
                                                    .indexOf(serial));
                                            cnt++;
                                            break;
                                        }
                                    }
                                }
                            }

                            // Add actor to the list if rating nonzero
                            if (cnt != 0) {
                                result /= cnt;
                                actorRatings.add(result);
                                copyActors.add(actor.getName());
                            }
                        }

                        // Sort actors by rating
                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {

                                if (((Double.compare(actorRatings.get(i),
                                        actorRatings.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((Double.compare(actorRatings.get(i),
                                        actorRatings.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))
                                        || (((Double.compare(actorRatings.get(i),
                                        actorRatings.get(j)) == 0))
                                        && (((copyActors.get(i).compareTo(copyActors.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyActors.get(i).compareTo(copyActors.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // For each actor calculate their total awards count
                        // and search for all filter awards
                        for (ActorInputData actor : input.getActors()) {
                            result = 0;

                            for (Map.Entry<ActorsAwards, Integer> element
                                    : actor.getAwards().entrySet()) {
                                result += element.getValue();
                            }

                            ok = true;
                            for (String searchedAward
                                    : action.getFilters().get(Constants.AWARDS_INDEX)) {
                                if (!(actor.getAwards().containsKey(Utils
                                        .stringToAwards(searchedAward)))) {
                                    ok = false;
                                    break;
                                }
                            }

                            // If actor has all awards put into list
                            if (ok) {
                                awardsCount.add(result);
                                copyActors.add(actor.getName());
                            }
                        }

                        // Sort by total awards count
                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {

                                if (((awardsCount.get(i) < awardsCount.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((awardsCount.get(i) > awardsCount.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((awardsCount.get(i).equals(awardsCount.get(j))))
                                        && (((copyActors.get(i).compareTo(copyActors.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyActors.get(i).compareTo(copyActors.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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
                        boolean auxiliaryOk;

                        // For all actors search filter words in their description
                        for (ActorInputData actor : input.getActors()) {
                            description = actor.getCareerDescription().toLowerCase();
                            auxiliaryOk = true;
                            for (String searchedWord
                                    : action.getFilters().get(Constants.WORDS_INDEX)) {
                                ok = false;
                                for (String word : description.split("[? .!+/,\n-]+")) {
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

                            // If all words are found add to list
                            if (auxiliaryOk) {
                                copyActors.add(actor.getName());
                            }
                        }

                        // Sort alphabetically
                        for (int i = 0; i < copyActors.size() - 1; i++) {
                            for (int j = i + 1; j < copyActors.size(); j++) {


                                if (((copyActors.get(i).compareTo(copyActors.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyActors.get(i).compareTo(copyActors.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))) {

                                    stringAux = copyActors.get(i);
                                    copyActors.set(i, copyActors.get(j));
                                    copyActors.set(j, stringAux);
                                }
                            }
                        }
                    }
                    default -> System.out.println("Incorrect query");
                }

                // For all cases at the end take the minimum between
                // the maximum length of elements and the required amount
                // from the list
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
                String year = action.getFilters().get(Constants.YEAR_INDEX).get(0);
                String genre = action.getFilters().get(Constants.GENRE_INDEX).get(0);

                switch (action.getCriteria()) {
                    case "ratings" -> {
                        double result, doubleAux;
                        ArrayList<Double> movieRatings = new ArrayList<>();

                        // Calculate the ratings for each movie
                        for (MovieInputData movie : input.getMovies()) {
                            result = 0.0;
                            cnt = 0;
                            for (Double rating : movie.getRatings()) {
                                if (!(rating.equals(0.0))) {
                                    result += rating;
                                    cnt++;
                                }
                            }

                            // Add to list if non 0 and respects filters
                            if (((year == null) || (movie.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    result /= cnt;
                                    movieRatings.add(result);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        // Sort movies by rating
                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {

                                if (((Double.compare(movieRatings.get(i),
                                        movieRatings.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((Double.compare(movieRatings.get(i),
                                        movieRatings.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))
                                        || (((Double.compare(movieRatings.get(i),
                                        movieRatings.get(j)) == 0))
                                        && (((copyMovies.get(i)
                                        .compareTo(copyMovies.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // Count number of times movie appears as fav
                        for (MovieInputData movie : input.getMovies()) {
                            cnt = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getFavoriteMovies().contains(movie.getTitle())) {
                                    cnt++;
                                }
                            }

                            // Add if nonzero
                            if (((year == null) || (movie.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    movieCount.add(cnt);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        // Sort by number of appearances
                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {

                                if (((movieCount.get(i) < movieCount.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((movieCount.get(i) > movieCount.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((movieCount.get(i).equals(movieCount.get(j))))
                                        && (((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // Add movie lengths to list
                        for (MovieInputData movie : input.getMovies()) {
                            if (((year == null) || (movie.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                movieDuration.add(movie.getDuration());
                                copyMovies.add(movie.getTitle());
                            }
                        }

                        // Sort by duration
                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {

                                if (((movieDuration.get(i) < movieDuration.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((movieDuration.get(i) > movieDuration.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((movieDuration.get(i).equals(movieDuration.get(j))))
                                        && (((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // Calculate view count for each movie
                        for (MovieInputData movie : input.getMovies()) {
                            result = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getHistory().containsKey(movie.getTitle())) {
                                    result += user.getHistory().get(movie.getTitle());
                                }
                            }

                            // Add to list if filters apply
                            if (((year == null) || (movie.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (movie.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    movieCount.add(result);
                                    copyMovies.add(movie.getTitle());
                                }
                            }
                        }

                        // Sort by counter
                        for (int i = 0; i < copyMovies.size() - 1; i++) {
                            for (int j = i + 1; j < copyMovies.size(); j++) {

                                if (((movieCount.get(i) < movieCount.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((movieCount.get(i) > movieCount.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((movieCount.get(i).equals(movieCount.get(j))))
                                        && (((copyMovies.get(i).compareTo(copyMovies.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyMovies.get(i).compareTo(copyMovies.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                // In all cases take as many videos as possible, up to the number required
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
                String year = action.getFilters().get(Constants.YEAR_INDEX).get(0);
                String genre = action.getFilters().get(Constants.GENRE_INDEX).get(0);

                switch (action.getCriteria()) {
                    case "ratings" -> {
                        double result, seasonResult, doubleAux;
                        ArrayList<Double> serialRatings = new ArrayList<>();

                        // Calculate ratings for each show by sum of seasons
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

                            // Add rating to list if filters apply and nonzero
                            if (((year == null) || (serial.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    result /= serial.getNumberSeason();
                                    serialRatings.add(result);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        // Sort by ratings
                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {

                                if (((Double.compare(serialRatings.get(i),
                                        serialRatings.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((Double.compare(serialRatings.get(i),
                                        serialRatings.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))
                                        || (((Double.compare(serialRatings.get(i),
                                        serialRatings.get(j)) == 0))
                                        && (((copyShows.get(i)
                                        .compareTo(copyShows.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyShows.get(i).compareTo(copyShows.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // Count times it's a favourite video
                        for (SerialInputData serial : input.getSerials()) {
                            cnt = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getFavoriteMovies().contains(serial.getTitle())) {
                                    cnt++;
                                }
                            }

                            // Add to list
                            if (((year == null) || (serial.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (cnt != 0) {
                                    serialCount.add(cnt);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        // Sort by times favourite
                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {

                                if (((serialCount.get(i) < serialCount.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((serialCount.get(i) > serialCount.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((serialCount.get(i).equals(serialCount.get(j))))
                                        && (((copyShows.get(i).compareTo(copyShows.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyShows.get(i).compareTo(copyShows.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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
                        int  intAux, totalDuration;
                        ArrayList<Integer> serialDuration = new ArrayList<>();

                        // Calculate duration by sum of serials and add to list
                        for (SerialInputData serial : input.getSerials()) {
                            totalDuration = 0;
                            if (((year == null) || (serial.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                for (int i = 0; i < serial.getNumberSeason(); i++) {
                                    totalDuration += serial.getSeasons().get(i).getDuration();
                                }
                                serialDuration.add(totalDuration);
                                copyShows.add(serial.getTitle());
                            }
                        }

                        // Sort by duration
                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {

                                if (((serialDuration.get(i) < serialDuration.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((serialDuration.get(i) > serialDuration.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((serialDuration.get(i).equals(serialDuration.get(j))))
                                        && (((copyShows.get(i).compareTo(copyShows.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyShows.get(i).compareTo(copyShows.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                        // Count numbers it was viewed
                        for (SerialInputData serial : input.getSerials()) {
                            result = 0;
                            for (UserInputData user : input.getUsers()) {
                                if (user.getHistory().containsKey(serial.getTitle())) {
                                    result += user.getHistory().get(serial.getTitle());
                                }
                            }

                            // Add to list
                            if (((year == null) || (serial.getYear() == Integer.parseInt(year)))
                                    && ((genre == null) || (serial.getGenres().contains(genre)))) {
                                if (result != 0) {
                                    serialCount.add(result);
                                    copyShows.add(serial.getTitle());
                                }
                            }
                        }

                        // Sort by favourite count
                        for (int i = 0; i < copyShows.size() - 1; i++) {
                            for (int j = i + 1; j < copyShows.size(); j++) {

                                if (((serialCount.get(i) < serialCount.get(j))
                                        && (action.getSortType().equals("desc")))
                                        || ((serialCount.get(i) > serialCount.get(j))
                                        && (action.getSortType().equals("asc")))
                                        || (((serialCount.get(i).equals(serialCount.get(j))))
                                        && (((copyShows.get(i).compareTo(copyShows.get(j)) < 0)
                                        && (action.getSortType().equals("desc")))
                                        || ((copyShows.get(i).compareTo(copyShows.get(j)) > 0)
                                        && (action.getSortType().equals("asc")))))) {

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

                // In all cases take as many videos as possible, up to the number required
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

                // For each user count number of times they rated
                for (UserInputData user : input.getUsers()) {
                    cnt = 0;
                    for (MovieInputData movie : input.getMovies()) {
                        if (!(movie.getRatings().get(input.getUsers().indexOf(user))
                                .equals(0.0))) {
                            cnt++;
                        }
                    }

                    for (SerialInputData serial : input.getSerials()) {
                        for (Season season : serial.getSeasons()) {
                            if (!(season.getRatings().get(input.getUsers().indexOf(user))
                                    .equals(0.0))) {
                                cnt++;
                            }
                        }
                    }

                    if (cnt != 0) {
                        userRatings.add(cnt);
                        copyUsers.add(user.getUsername());
                    }
                }

                // Sort by number of times they rated
                for (int i = 0; i < copyUsers.size() - 1; i++) {
                    for (int j = i + 1; j < copyUsers.size(); j++) {

                        if (((userRatings.get(i) < userRatings.get(j))
                                && (action.getSortType().equals("desc")))
                                || ((userRatings.get(i) > userRatings.get(j))
                                && (action.getSortType().equals("asc")))
                                || (((userRatings.get(i).equals(userRatings.get(j))))
                                && (((copyUsers.get(i).compareTo(copyUsers.get(j)) < 0)
                                && (action.getSortType().equals("desc")))
                                || ((copyUsers.get(i).compareTo(copyUsers.get(j)) > 0)
                                && (action.getSortType().equals("asc")))))) {
                            intAux = userRatings.get(i);
                            userRatings.set(i, userRatings.get(j));
                            userRatings.set(j, intAux);

                            stringAux = copyUsers.get(i);
                            copyUsers.set(i, copyUsers.get(j));
                            copyUsers.set(j, stringAux);
                        }
                    }
                }

                // Add to list the appropriate number of users
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
    @SuppressWarnings("unchecked")
    public void processRecommendation(final ActionInputData action) {
        JSONObject object = new JSONObject();
        object.put(Constants.ID_STRING, action.getActionId());
        StringBuilder message = new StringBuilder();

        boolean ok;

        // Search the user that launched the action
        UserInputData user = null;
        for (UserInputData iterator : input.getUsers()) {
            if (iterator.getUsername().equals(action.getUsername())) {
                user = iterator;
                break;
            }
        }

        switch (action.getType()) {
            case "standard" -> {
                ok = true;
                if (user == null) {
                    message.append("StandardRecommendation cannot be applied!");
                    break;
                }

                // Search first movie available
                for (MovieInputData movie : input.getMovies()) {
                    if (!(user.getHistory().containsKey(movie.getTitle()))) {
                        ok = false;
                        message.append("StandardRecommendation result: ");
                        message.append(movie.getTitle());
                        break;
                    }
                }

                // Search first serial available
                for (SerialInputData serial : input.getSerials()) {
                    if (!(user.getHistory().containsKey(serial.getTitle())) && ok) {
                        ok = false;
                        message.append("StandardRecommendation result: ");
                        message.append(serial.getTitle());
                        break;
                    }
                }

                if (ok) {
                    message.append("StandardRecommendation cannot be applied!");
                }
            }
            case "best_unseen" -> {
                int cnt;
                double result, seasonRating, currentBestRating = -1.0;
                String currentBestVideo = "";
                ok = true;

                if (user == null) {
                    message.append("BestRatedUnseenRecommendation cannot be applied!");
                    break;
                }

                // Calculate movie ratings and step by step find the maximum rating of unseen movie
                for (MovieInputData movie : input.getMovies()) {
                    cnt = 0;
                    result = 0.0;
                    for (Double movieRating : movie.getRatings()) {
                        if (!(movieRating.equals(0.0))) {
                            result += movieRating;
                            cnt++;
                        }
                        if (cnt != 0) {
                            result /= cnt;
                        }
                    }

                    if (!(user.getHistory().containsKey(movie.getTitle()))) {
                        ok = false;
                        if (Double.compare(currentBestRating, result) < 0) {
                            currentBestRating = result;
                            currentBestVideo = movie.getTitle();
                        }
                    }
                }

                // Calculate serial ratings and step by step find the best rating of unseen serials
                for (SerialInputData serial : input.getSerials()) {
                    cnt = 0;
                    result = 0.0;
                    for (Season season : serial.getSeasons()) {
                        seasonRating = 0.0;
                        for (Double showRating : season.getRatings()) {
                            if (!(showRating.equals(0.0))) {
                                seasonRating += showRating;
                                cnt++;
                            }
                            if (cnt != 0) {
                                seasonRating /= cnt;
                            }
                        }
                        result += seasonRating;
                    }
                    result /= serial.getNumberSeason();

                    if (!(user.getHistory().containsKey(serial.getTitle()))) {
                        ok = false;
                        if (Double.compare(currentBestRating, result) < 0) {
                            currentBestRating = result;
                            currentBestVideo = serial.getTitle();
                        }
                    }
                }

                // Check if any video was found
                if (ok) {
                    message.append("BestRatedUnseenRecommendation cannot be applied!");
                } else {
                    message.append("BestRatedUnseenRecommendation result: ");
                    message.append(currentBestVideo);
                }
            }
            case "popular" -> {
                int cnt, intAux;
                String stringAux;
                boolean hasGenre;
                ArrayList<Integer> popularity = new ArrayList<>();
                ArrayList<String> copyGenres = new ArrayList<>();

                if (user == null || user.getSubscriptionType().equals("BASIC")) {
                    message.append("PopularRecommendation cannot be applied!");
                    break;
                }

                // For each genre calculate popularity for movies
                for (Genre genre : Genre.values()) {
                    cnt = 0;
                    copyGenres.add(genre.toString());

                    for (MovieInputData movie : input.getMovies()) {
                        hasGenre = false;
                        for (String movieGenre : movie.getGenres()) {
                            if (genre.equals(Utils.stringToGenre(movieGenre))) {
                                hasGenre = true;
                                break;
                            }
                        }
                        if (hasGenre) {
                            for (UserInputData userIterator : input.getUsers()) {
                                if (userIterator.getHistory().containsKey(movie.getTitle())) {
                                    cnt += userIterator.getHistory().get(movie.getTitle());
                                }
                            }
                        }
                    }

                    // For each genre calculate popularity for serials
                    for (SerialInputData serial : input.getSerials()) {
                        hasGenre = false;
                        for (String serialGenre : serial.getGenres()) {
                            if (genre.equals(Utils.stringToGenre(serialGenre))) {
                                hasGenre = true;
                                break;
                            }
                        }
                        if (hasGenre) {
                            for (UserInputData userIterator : input.getUsers()) {
                                if (userIterator.getHistory().containsKey(serial.getTitle())) {
                                    cnt += userIterator.getHistory().get(serial.getTitle());
                                }
                            }
                        }
                    }
                    popularity.add(cnt);
                }

                // Sort genres by populairty
                for (int i = 0; i < copyGenres.size() - 1; i++) {
                    for (int j = i + 1; j < copyGenres.size(); j++) {
                        if (popularity.get(i) < popularity.get(j)) {
                            intAux = popularity.get(i);
                            popularity.set(i, popularity.get(j));
                            popularity.set(j, intAux);

                            stringAux = copyGenres.get(i);
                            copyGenres.set(i, copyGenres.get(j));
                            copyGenres.set(j, stringAux);
                        }
                    }
                }

                ok = true;

                // For each genre search all videos of that genre, and take the first unseen
                for (String genre : copyGenres) {
                    if (!ok) {
                        break;
                    }

                    for (MovieInputData movie : input.getMovies()) {
                        for (String movieGenre : movie.getGenres()) {
                            if (movieGenre.toLowerCase().equals(genre.toLowerCase())) {
                                if (!user.getHistory().containsKey(movie.getTitle()) && ok) {
                                    ok = false;
                                    message.append("PopularRecommendation result: ");
                                    message.append(movie.getTitle());
                                    break;
                                }
                            }
                        }
                    }

                    for (SerialInputData serial : input.getSerials()) {
                        for (String serialGenre : serial.getGenres()) {
                            if (serialGenre.toLowerCase().equals(genre.toLowerCase())) {
                                if (!user.getHistory().containsKey(serial.getTitle()) && ok) {
                                    ok = false;
                                    message.append("PopularRecommendation result: ");
                                    message.append(serial.getTitle());
                                    break;
                                }
                            }
                        }
                    }
                }

                // If no suitable video was found
                if (ok) {
                    message.append("PopularRecommendation cannot be applied!");
                }
            }
            case "favorite" -> {
                int cnt, currentMostNum = 0;
                String currentMostFav = "";
                ok = true;

                if (user == null || user.getSubscriptionType().equals("BASIC")) {
                    message.append("FavoriteRecommendation cannot be applied!");
                    break;
                }

                // Count times movies appear as favourites
                for (MovieInputData movie : input.getMovies()) {
                    cnt = 0;
                    for (UserInputData userIterator : input.getUsers()) {
                        if (userIterator.getFavoriteMovies().contains(movie.getTitle())) {
                            cnt++;
                        }
                    }

                    if (!(user.getHistory().containsKey(movie.getTitle())) && cnt != 0) {
                        ok = false;
                        if (currentMostNum < cnt) {
                            currentMostNum = cnt;
                            currentMostFav = movie.getTitle();
                        }
                    }
                }

                // Count times serials appear as favourites
                for (SerialInputData serial : input.getSerials()) {
                    cnt = 0;
                    for (UserInputData userIterator : input.getUsers()) {
                        if (userIterator.getFavoriteMovies().contains(serial.getTitle())) {
                            cnt++;
                        }
                    }

                    if (!(user.getHistory().containsKey(serial.getTitle())) && cnt != 0) {
                        ok = false;
                        if (currentMostNum < cnt) {
                            currentMostNum = cnt;
                            currentMostFav = serial.getTitle();
                        }
                    }
                }

                // If no video unseen was found
                if (ok) {
                    message.append("FavoriteRecommendation cannot be applied!");
                } else {
                    message.append("FavoriteRecommendation result: ");
                    message.append(currentMostFav);
                }
            }
            case "search" -> {
                int cnt;
                double result, seasonRating, doubleAux;
                String stringAux;
                ok = true;
                ArrayList<String> copyVideos = new ArrayList<>();
                ArrayList<Double> videoRatings = new ArrayList<>();

                if (user == null || user.getSubscriptionType().equals("BASIC")) {
                    message.append("SearchRecommendation cannot be applied!");
                    break;
                }

                // Calculate movie ratings and add if criteria is met and unseen
                for (MovieInputData movie : input.getMovies()) {
                    cnt = 0;
                    result = 0.0;
                    for (Double movieRating : movie.getRatings()) {
                        if (!(movieRating.equals(0.0))) {
                            result += movieRating;
                            cnt++;
                        }
                    }
                    if (cnt != 0) {
                        result /= cnt;
                    }

                    if (movie.getGenres().contains(action.getGenre())
                            && !(user.getHistory().containsKey(movie.getTitle()))) {
                        ok = false;
                        copyVideos.add(movie.getTitle());
                        videoRatings.add(result);
                    }
                }

                // Calculate serial ratings and add if criteria is met and unseen
                for (SerialInputData serial : input.getSerials()) {
                    cnt = 0;
                    result = 0.0;
                    for (Season season : serial.getSeasons()) {
                        seasonRating = 0.0;
                        for (Double showRating : season.getRatings()) {
                            if (!(showRating.equals(0.0))) {
                                seasonRating += showRating;
                                cnt++;
                            }
                            if (cnt != 0) {
                                seasonRating /= cnt;
                            }
                        }
                        result += seasonRating;
                    }
                    result /= serial.getNumberSeason();

                    if (serial.getGenres().contains(action.getGenre())
                            && !(user.getHistory().containsKey(serial.getTitle()))) {
                        ok = false;
                        copyVideos.add(serial.getTitle());
                        videoRatings.add(result);
                    }
                }

                // Check if no unseen video of that criteria was found
                if (ok) {
                    message.append("SearchRecommendation cannot be applied!");
                } else {
                    message.append("SearchRecommendation result: [");

                    // Sort by ratings
                    for (int i = 0; i < copyVideos.size() - 1; i++) {
                        for (int j = i + 1; j < copyVideos.size(); j++) {

                            if (Double.compare(videoRatings.get(i), videoRatings.get(j)) > 0
                                    || (videoRatings.get(i).equals(videoRatings.get(j))
                                    && (copyVideos.get(i).compareTo(copyVideos.get(j)) > 0))) {

                                doubleAux = videoRatings.get(i);
                                videoRatings.set(i, videoRatings.get(j));
                                videoRatings.set(j, doubleAux);

                                stringAux = copyVideos.get(i);
                                copyVideos.set(i, copyVideos.get(j));
                                copyVideos.set(j, stringAux);
                            }
                        }
                    }

                    // Add all the videos found
                    for (int i = 0; i < copyVideos.size() - 1; i++) {
                        message.append(copyVideos.get(i));
                        message.append(", ");
                    }
                    if (copyVideos.size() > 0) {
                        message.append(copyVideos.get(copyVideos.size() - 1));
                    }

                    message.append("]");
                }
            }
            default -> System.out.println("Incorrect recommendation");
        }
        object.put(Constants.MESSAGE, message.toString());
        arrayResult.add(object);
    }
}
