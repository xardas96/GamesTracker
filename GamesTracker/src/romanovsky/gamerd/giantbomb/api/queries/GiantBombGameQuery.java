package romanovsky.gamerd.giantbomb.api.queries;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import romanovsky.gamerd.giantbomb.api.core.Game;

public class GiantBombGameQuery extends AbstractGiantBombQuery<String> {
	
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
	protected List<String> parseResponse(Element root, boolean untilToday) {
		List<String> responseGenres = new ArrayList<String>();
		Node genresNode = root.selectSingleNode("//genres");
		@SuppressWarnings("unchecked")
		List<Node> genresNodes = genresNode.selectNodes("genre");
		for (Node genreNode : genresNodes) {
			responseGenres.add(genreNode.selectSingleNode("name").getText());
		}
		return responseGenres;
	}
}
