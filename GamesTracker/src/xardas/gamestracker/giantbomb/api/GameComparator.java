package xardas.gamestracker.giantbomb.api;

import java.util.Comparator;

import org.joda.time.DateTime;

public class GameComparator implements Comparator<Game> {

	@Override
	public int compare(Game lhs, Game rhs) {
		DateTime rel1 = lhs.getReleaseDate();
		DateTime rel2 = rhs.getReleaseDate();
		if (rel1.compareTo(rel2) == 0) {
			int quarters = lhs.getExpectedReleaseQuarter() - rhs.getExpectedReleaseQuarter();
			if (quarters == 0) {
				return lhs.getName().compareTo(rhs.getName());
			}
			return quarters;
		}
		return rel1.compareTo(rel2);
	}
}