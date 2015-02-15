package romanovsky.gamerdplus.giantbomb.api;

import java.util.Comparator;

import org.joda.time.DateTime;

import romanovsky.gamerdplus.giantbomb.api.core.Game;

public class GameReleaseDateComparator implements Comparator<Game> {

	@Override
	public int compare(Game lhs, Game rhs) {
		int compare = 0;
		DateTime rel1 = lhs.getReleaseDate();
		DateTime rel2 = rhs.getReleaseDate();
		if (rel1.getYear() == 0) {
			if (rel2.getYear() == 0) {
				compare = compareNames(lhs, rhs);
			} else {
				compare = 1;
			}
		} else if (rel2.getYear() == 0) {
			compare = -1;
		} else if (rel2.getYear() != 0) {
			compare = rel1.compareTo(rel2);
			if (compare == 0) {
				int q1 = lhs.getExpectedReleaseQuarter();
				int q2 = rhs.getExpectedReleaseQuarter();
				if (q1 != 0) {
					if (q2 != 0) {
						compare = q1 - q2;
						if (compare == 0) {
							compare = compareNames(lhs, rhs);
						}
					} else {
						compare = -1;
					}
				} else if (q2 == 0) {
					compare = compareNames(lhs, rhs);
				} else if (q2 != 0) {
					compare = 1;
				}
			}
		}
		return compare;
	}

	private int compareNames(Game lhs, Game rhs) {
		String n1 = lhs.getName();
		String n2 = rhs.getName();
		return n1.compareTo(n2);
	}
}