package xardas.gamestracker.giantbomb.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class GiantBombGamesQuery {
	private static final String URL = "http://www.giantbomb.com/api/games/";
	private String apiKey;
	private Map<FilterEnum, String> filters;
	private SimpleDateFormat sdf;

	public GiantBombGamesQuery() {
		filters = new HashMap<FilterEnum, String>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	}

	public GiantBombGamesQuery setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public GiantBombGamesQuery addFilter(FilterEnum field, String value) {
		filters.put(field, value);
		return this;
	}

	public List<Game> execute() throws Exception {
		URL url = buildQuery();
		InputStream is = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		Document doc = DocumentHelper.parseText(sb.toString());
		return parseResponse(doc);

	}

	private List<Game> parseResponse(Document xmlResponse) {
		List<Game> result = new ArrayList<Game>();
		Element root = xmlResponse.getRootElement();
		String statusValue = root.selectSingleNode("status_code").getText();
		int status = Integer.parseInt(statusValue);
		if (status == 1) {
			@SuppressWarnings("unchecked")
			List<Node> games = root.selectNodes("//game");
			for (Node gameNode : games) {
				Game game = new Game();
				String dateLastUpdated = gameNode.selectSingleNode("date_last_updated").getText();
				long time;
				try {
					time = sdf.parse(dateLastUpdated).getTime();
				} catch (ParseException e) {
					time = 0;
				}
				game.setDateLastUpdated(time);
				String expectedReleaseDay = gameNode.selectSingleNode("expected_release_day").getText();
				game.setExpectedReleaseDay(expectedReleaseDay.equals("") ? 0 : Integer.valueOf(expectedReleaseDay));
				String expectedReleaseMonth = gameNode.selectSingleNode("expected_release_month").getText();
				game.setExpectedReleaseMonth(expectedReleaseMonth.equals("") ? 0 : Integer.valueOf(expectedReleaseMonth));
				String expectedReleaseQuarter = gameNode.selectSingleNode("expected_release_quarter").getText();
				game.setExpectedReleaseQuarter(expectedReleaseQuarter);
				String expectedReleaseYear = gameNode.selectSingleNode("expected_release_year").getText();
				game.setExpectedReleaseYear(expectedReleaseYear.equals("") ? 0 : Integer.valueOf(expectedReleaseYear));
				String id = gameNode.selectSingleNode("id").getText();
				game.setId(Long.valueOf(id));
				Node iconNode = gameNode.selectSingleNode("image/icon_url");
				Node smallNode = gameNode.selectSingleNode("image/small_url");
				if (iconNode != null) {
					String iconURL = iconNode.getText();
					game.setIconURL(iconURL);
				}
				if (smallNode != null) {
					String smallURL = smallNode.getText();
					game.setSmallURL(smallURL);
				}
				String name = gameNode.selectSingleNode("name").getText();
				game.setName(name);
				@SuppressWarnings("unchecked")
				List<Node> platforms = gameNode.selectNodes("platforms/platform");
				List<String> platformsList = new ArrayList<String>();
				for (Node platform : platforms) {
					platformsList.add(platform.selectSingleNode("abbreviation").getText());
				}
				game.setPlatforms(platformsList);
				result.add(game);
			}
		}
		Collections.sort(result, new Comparator<Game>() {
			@Override
			public int compare(Game lhs, Game rhs) {
				if (lhs.getExpectedReleaseYear() == rhs.getExpectedReleaseYear()) {
					if (lhs.getExpectedReleaseMonth() == rhs.getExpectedReleaseMonth()) {
						if (lhs.getExpectedReleaseDay() == rhs.getExpectedReleaseDay()) {
							return lhs.getName().compareTo(rhs.getName());
						} else {
							return lhs.getExpectedReleaseDay() - rhs.getExpectedReleaseDay();
						}
					} else {
						return lhs.getExpectedReleaseMonth() - rhs.getExpectedReleaseMonth();
					}
				} else {
					return lhs.getExpectedReleaseYear() - rhs.getExpectedReleaseYear();
				}
			}
		});
		return result;
	}

	private URL buildQuery() throws MalformedURLException {
		StringBuilder sb = new StringBuilder(URL);
		sb.append("?api_key=").append(apiKey);
		sb.append("&filter=");
		for (FilterEnum field : filters.keySet()) {
			String value = filters.get(field);
			sb.append(field.name()).append(":").append(value).append(",");
		}
		if (!filters.isEmpty()) {
			sb.setLength(sb.length() - 1);
		}
		URL url = new URL(sb.toString());
		return url;
	}
}