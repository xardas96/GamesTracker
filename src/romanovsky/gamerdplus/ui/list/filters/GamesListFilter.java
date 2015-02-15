package romanovsky.gamerdplus.ui.list.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import romanovsky.gamerdplus.giantbomb.api.GameReleaseDateComparator;
import romanovsky.gamerdplus.giantbomb.api.core.Game;
import romanovsky.gamerdplus.ui.list.GamesListExpandableAdapter;
import android.util.Pair;
import android.widget.Filter;

public class GamesListFilter extends Filter {
	private GamesListExpandableAdapter adapter;

	public GamesListFilter(GamesListExpandableAdapter adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();
		if (constraint.equals("")) {
			results.values = new Pair<List, List>(new ArrayList<Game>(adapter.getGamesForFilter()), new ArrayList<Game>(adapter.getOutGamesForFilter()));
		} else {
			String[] split = constraint.toString().split(";");
			String platforms = split[0];
			String genres = split[1];
			List<String> platformsToFilter = new ArrayList<String>();
			String[] platformsSplit = platforms.split(":");
			if (platformsSplit.length > 1) {
				String[] platformsArray = platformsSplit[1].split(",");
				platformsToFilter = Arrays.asList(platformsArray);
			}
			List<String> genresToFilter = new ArrayList<String>();
			String[] genresSplit = genres.split(":");
			if (genresSplit.length > 1) {
				String[] genresArray = genresSplit[1].split(",");
				genresToFilter = Arrays.asList(genresArray);
			}
			Set<Game> games = filter(adapter.getGamesForFilter(), platformsToFilter, genresToFilter);
			Set<Game> out = filter(adapter.getOutGamesForFilter(), platformsToFilter, genresToFilter);
			results.values = new Pair<List, List>(new ArrayList<Game>(games), new ArrayList<Game>(out));
		}
		return results;
	}

	private Set<Game> filter(List<Game> games, List<String> platformsToFilter, List<String> genresToFilter) {
		Set<Game> result = new HashSet<Game>();
		for (Game game : games) {
			List<String> gamePlatforms = game.getPlatforms();
			List<String> gameGenres = game.getGenres();
			boolean platformsFiltered = false;
			if (!platformsToFilter.isEmpty()) {
				platformsFiltered = Collections.disjoint(gamePlatforms, platformsToFilter);
			}
			boolean genresFiltered = false;
			if (!genresToFilter.isEmpty()) {
				genresFiltered = Collections.disjoint(gameGenres, genresToFilter);
			}
			if (!platformsFiltered && !genresFiltered) {
				result.add(game);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		Pair<List, List> pair = (Pair) results.values;
		if (pair != null) {
			adapter.setGames(pair.first);
			adapter.setOutGames(pair.second);
			adapter.sort(new GameReleaseDateComparator());
			adapter.notifyDataSetChanged();
		}
	}
}
