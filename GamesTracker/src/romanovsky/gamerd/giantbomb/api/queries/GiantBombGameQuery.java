package romanovsky.gamerd.giantbomb.api.queries;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import romanovsky.gamerd.giantbomb.api.core.Game;
import romanovsky.gamerd.giantbomb.api.core.Genre;

public class GiantBombGameQuery extends AbstractGiantBombQuery<Genre> {
	
	public GiantBombGameQuery(Game game) {
		super("http://www.giantbomb.com/api/game/" + game.getId() + "/");
		fields = new String[] {
				"genres"
		}; 
	}

	@Override
	protected void appendInfo(StringBuilder infoBuilder) {
		infoBuilder.append("&field_list=");
		for (String field : fields) {
			infoBuilder.append(field).append(",");
		}
		if (fields.length != 0) {
			infoBuilder.setLength(infoBuilder.length() - 1);
		}
	}

	@Override
	protected List<Genre> parseResponse(Element root, boolean untilToday) {
		List<Genre> responseGenres = new ArrayList<Genre>();
		Node genresNode = root.selectSingleNode("//genres");
		if (genresNode != null) {
			@SuppressWarnings("unchecked")
			List<Node> genresNodes = genresNode.selectNodes("genre");
			for (Node genreNode : genresNodes) {
				Genre genre = new Genre();
				String genreName = genreNode.selectSingleNode("name").getText();
				genre.setName(genreName);
				responseGenres.add(genre);
			}
		}
		return responseGenres;
	}
}
