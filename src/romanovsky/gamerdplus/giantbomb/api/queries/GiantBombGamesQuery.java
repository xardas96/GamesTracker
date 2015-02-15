package romanovsky.gamerdplus.giantbomb.api.queries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Node;
import org.joda.time.DateTime;

import romanovsky.gamerdplus.giantbomb.api.FilterEnum;
import romanovsky.gamerdplus.giantbomb.api.core.Game;
import romanovsky.gamerdplus.giantbomb.api.core.Platform;

public class GiantBombGamesQuery extends AbstractGiantBombQuery<Game> {
	private Map<String, String> filters;
	private SimpleDateFormat sdf;
	private Set<Platform> discoveredPlatforms;
	
	public GiantBombGamesQuery() {
		super("http://www.giantbomb.com/api/games/");
		fields = new String[] { 
				"id"
				, "date_last_updated"
				, "expected_release_day"
				, "date_last_updated"
				, "original_release_date"
				, "expected_release_month"
				, "expected_release_quarter"
				, "expected_release_year"
				, "image"
				, "name"
				, "platforms"
				, "deck"
				, "site_detail_url"
				, "api_detail_url"
				};
		filters = new HashMap<String, String>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		discoveredPlatforms = new HashSet<Platform>();
	}

	public GiantBombGamesQuery addFilter(FilterEnum field, String value) {
		filters.put(field.name(), value);
		return this;
	}

	public Set<Platform> getDiscoveredPlatforms() {
		return discoveredPlatforms;
	}

	protected List<Game> parseResponse(Element root, boolean untilToday) {
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
			final String id = gameNode.selectSingleNode("id").getText();
			game.setId(Long.valueOf(id));
			String apiDetailUrl = gameNode.selectSingleNode("api_detail_url").getText();
			game.setApiDetailURL(apiDetailUrl);
			Node iconNode = gameNode.selectSingleNode("image/icon_url");
			if (iconNode != null) {
				String iconURL = iconNode.getText();
				game.setIconURL(iconURL);
			}
			String siteDetailURL = gameNode.selectSingleNode("site_detail_url").getText();
			game.setSiteDetailURL(siteDetailURL);
			String name = gameNode.selectSingleNode("name").getText();
			game.setName(name);
			@SuppressWarnings("unchecked")
			List<Node> platforms = gameNode.selectNodes("platforms/platform");
			List<String> platformsList = new ArrayList<String>();
			for (Node platform : platforms) {
				Platform platf = new Platform();
				String platformName = platform.selectSingleNode("name").getText();
				String platformAbbreviation = platform.selectSingleNode("abbreviation").getText();
				platf.setName(platformName);
				platf.setAbbreviation(platformAbbreviation);
				discoveredPlatforms.add(platf);
				platformsList.add(platformAbbreviation);
			}
			Collections.sort(platformsList);
			game.setPlatforms(platformsList);
			String description = gameNode.selectSingleNode("deck").getText();
			game.setDescription(description);
			DateTime now = new DateTime();
			int year = now.getYear();
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
			} else if (originalReleaseDate.equals(year + "-01-01 00:00:00")) {
				game.setExpectedReleaseYear(year);
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

	@Override
	protected void appendInfo(StringBuilder infoBuilder) {
		infoBuilder.append("&filter=");
		for (String field : filters.keySet()) {
			String value = filters.get(field);
			infoBuilder.append(field).append(":").append(value).append(",");
		}
		if (!filters.isEmpty()) {
			infoBuilder.setLength(infoBuilder.length() - 1);
		}
		infoBuilder.append("&field_list=");
		for (String field : fields) {
			infoBuilder.append(field).append(",");
		}
		if (fields.length != 0) {
			infoBuilder.setLength(infoBuilder.length() - 1);
		}
	}
}