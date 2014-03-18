package romanovsky.gamerd.giantbomb.api;

import romanovsky.gamerd.giantbomb.api.core.Game;
import romanovsky.gamerd.giantbomb.api.queries.GiantBombGameQuery;
import romanovsky.gamerd.giantbomb.api.queries.GiantBombGamesQuery;

public abstract class GiantBombApi {
	private static String apiKey;

	public static void setApiKey(String apiKey) {
		GiantBombApi.apiKey = apiKey;
	}

	public static GiantBombGamesQuery createGamesQuery() {
		GiantBombGamesQuery query = new GiantBombGamesQuery();
		query.setApiKey(apiKey);
		return query;
	}

	public static GiantBombGameQuery createGameQuery(Game game) {
		GiantBombGameQuery query = new GiantBombGameQuery(game);
		query.setApiKey(apiKey);
		return query;
	}

}