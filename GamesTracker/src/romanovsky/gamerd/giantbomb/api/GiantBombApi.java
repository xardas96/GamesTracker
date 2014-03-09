package romanovsky.gamerd.giantbomb.api;

public abstract class GiantBombApi {
	private static String apiKey;

	public static void setApiKey(String apiKey) {
		GiantBombApi.apiKey = apiKey;
	}

	public static GiantBombGamesQuery createQuery() {
		GiantBombGamesQuery query = new GiantBombGamesQuery();
		query.setApiKey(apiKey);
		return query;
	}
}