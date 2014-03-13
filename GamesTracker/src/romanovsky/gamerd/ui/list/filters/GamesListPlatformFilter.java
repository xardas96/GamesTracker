package romanovsky.gamerd.ui.list.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import romanovsky.gamerd.giantbomb.api.GameReleaseDateComparator;
import romanovsky.gamerd.giantbomb.api.core.Game;
import romanovsky.gamerd.ui.list.GamesListExpandableAdapter;
import android.util.Pair;
import android.widget.Filter;

public class GamesListPlatformFilter extends Filter {
	private GamesListExpandableAdapter adapter;

	public GamesListPlatformFilter(GamesListExpandableAdapter adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();
		if (constraint.equals("")) {
			results.values = new Pair<List, List>(adapter.getGamesForFilter(), adapter.getOutGamesForFilter());
		} else {
			Set<Game> games = new HashSet<Game>();
			Set<Game> out = new HashSet<Game>();
			for (String filteredPlatform : constraint.toString().split(",")) {
				for (Game game : adapter.getGamesForFilter()) {
					if (game.getPlatforms().contains(filteredPlatform)) {
						games.add(game);
					}
				}
				for (Game game : adapter.getOutGamesForFilter()) {
					if (game.getPlatforms().contains(filteredPlatform)) {
						out.add(game);
					}
				}
			}
			results.values = new Pair<List, List>(new ArrayList<Game>(games), new ArrayList<Game>(out));
		}
		return results;
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
