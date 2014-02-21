package xardas.gamestracker.giantbomb.api;

import java.util.Comparator;
import java.util.Date;

public class GameComparator implements Comparator<Game> {

	@Override
	public int compare(Game lhs, Game rhs) {
		Date rel1 = lhs.getReleaseDate();
		Date rel2 = rhs.getReleaseDate();
		if (rel1.compareTo(rel2) == 0) {
			return lhs.getName().compareTo(rhs.getName());
		}
		return rel1.compareTo(rel2);
	}
}