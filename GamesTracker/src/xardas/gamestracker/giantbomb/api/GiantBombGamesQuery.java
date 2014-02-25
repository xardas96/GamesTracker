package xardas.gamestracker.giantbomb.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.joda.time.DateTime;

public class GiantBombGamesQuery {
	private static final String URL = "http://www.giantbomb.com/api/games/";
	private String apiKey;
	private Map<String, String> filters;
	private SimpleDateFormat sdf;
	private int offset = 0;
	private int limit = 20;
	private int totalResults = 1;
	private String[] fields = new String[] { "id", "date_last_updated", "expected_release_day", "date_last_updated", "original_release_date", "expected_release_month", "expected_release_quarter", "expected_release_year", "image", "name", "platforms" };

	public GiantBombGamesQuery() {
		filters = new HashMap<String, String>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		offset = 0;
	}

	public GiantBombGamesQuery setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public GiantBombGamesQuery addFilter(FilterEnum field, String value) {
		filters.put(field.name(), value);
		return this;
	}

	public List<Game> execute(boolean untilToday) throws Exception {
		List<Game> games = new ArrayList<Game>();
		Document doc = DocumentHelper.parseText(getResponse());
		Element root = doc.getRootElement();
		String statusValue = root.selectSingleNode("status_code").getText();
		int status = Integer.parseInt(statusValue);
		if (status == 1) {
			int results = Integer.valueOf(root.selectSingleNode("number_of_page_results").getText());
			totalResults = Integer.valueOf(root.selectSingleNode("number_of_total_results").getText());
			games.addAll(parseResponse(root, untilToday));
			offset += results;
		}
		return games;
	}

	public boolean reachedOffset() {
		return offset >= totalResults;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public String getResponse() throws Exception {
		URL url = buildQuery();
		InputStream is = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	private List<Game> parseResponse(Element root, boolean untilToday) {
		List<Game> result = new ArrayList<Game>();
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
			String originalReleaseDate = gameNode.selectSingleNode("original_release_date").getText();
			if (originalReleaseDate.equals("")) {
				String expectedReleaseDay = gameNode.selectSingleNode("expected_release_day").getText();
				game.setExpectedReleaseDay(expectedReleaseDay.equals("") ? 0 : Integer.valueOf(expectedReleaseDay));
				String expectedReleaseMonth = gameNode.selectSingleNode("expected_release_month").getText();
				game.setExpectedReleaseMonth(expectedReleaseMonth.equals("") ? 0 : Integer.valueOf(expectedReleaseMonth));
				String expectedReleaseQuarter = gameNode.selectSingleNode("expected_release_quarter").getText();
				game.setExpectedReleaseQuarter(expectedReleaseQuarter.equals("") ? 0 : Integer.valueOf(expectedReleaseQuarter));
				String expectedReleaseYear = gameNode.selectSingleNode("expected_release_year").getText();
				game.setExpectedReleaseYear(expectedReleaseYear.equals("") ? 0 : Integer.valueOf(expectedReleaseYear));
				result.add(game);
				// TODO giantbomb legacy
			} else if (originalReleaseDate.equals("2014-01-01 00:00:00")) {
				game.setExpectedReleaseYear(2014);
				result.add(game);
			} else if (!untilToday) {
				try {
					time = sdf.parse(originalReleaseDate).getTime();
				} catch (ParseException e) {
					time = 0;
				}
				DateTime relTime = new DateTime(time);
				game.setExpectedReleaseDay(relTime.getDayOfMonth());
				game.setExpectedReleaseMonth(relTime.getMonthOfYear());
				game.setExpectedReleaseYear(relTime.getYear());
				result.add(game);
			}
		}
		return result;
	}

	private URL buildQuery() throws MalformedURLException {
		StringBuilder sb = new StringBuilder(URL);
		sb.append("?api_key=").append(apiKey);
		sb.append("&filter=");
		for (String field : filters.keySet()) {
			String value = filters.get(field);
			sb.append(field).append(":").append(value).append(",");
		}
		if (!filters.isEmpty()) {
			sb.setLength(sb.length() - 1);
		}
		sb.append("&offset=").append(offset);
		sb.append("&limit=").append(limit);
		sb.append("&field_list=");
		for (String field : fields) {
			sb.append(field).append(",");
		}
		if (fields.length != 0) {
			sb.setLength(sb.length() - 1);
		}
		URL url = new URL(sb.toString());
		return url;
	}
}