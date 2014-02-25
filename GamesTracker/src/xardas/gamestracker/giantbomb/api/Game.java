package xardas.gamestracker.giantbomb.api;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class Game {
	private String name;
	private long id;
	private long dateLastUpdated;
	private int expectedReleaseDay;
	private int expectedReleaseMonth;
	private int expectedReleaseYear;
	private int expectedReleaseQuarter;
	private List<String> platforms;
	private String iconURL;
	private String smallURL;
	private boolean notify;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<String> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<String> platforms) {
		this.platforms = platforms;
	}

	public long getDateLastUpdated() {
		return dateLastUpdated;
	}

	public void setDateLastUpdated(long dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}

	public int getExpectedReleaseDay() {
		return expectedReleaseDay;
	}

	public void setExpectedReleaseDay(int expectedReleaseDay) {
		this.expectedReleaseDay = expectedReleaseDay;
	}

	public int getExpectedReleaseMonth() {
		return expectedReleaseMonth;
	}

	public void setExpectedReleaseMonth(int expectedReleaseMonth) {
		this.expectedReleaseMonth = expectedReleaseMonth;
	}

	public int getExpectedReleaseYear() {
		return expectedReleaseYear;
	}

	public void setExpectedReleaseYear(int expectedReleaseYear) {
		this.expectedReleaseYear = expectedReleaseYear;
	}

	public int getExpectedReleaseQuarter() {
		return expectedReleaseQuarter;
	}

	public void setExpectedReleaseQuarter(int expectedReleaseQuarter) {
		this.expectedReleaseQuarter = expectedReleaseQuarter;
	}

	public String getIconURL() {
		return iconURL;
	}

	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}

	public String getSmallURL() {
		return smallURL;
	}

	public void setSmallURL(String smallURL) {
		this.smallURL = smallURL;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public DateTime getReleaseDate() {
		DateTime release = new DateTime(expectedReleaseYear, expectedReleaseMonth == 0 ? 12 : expectedReleaseMonth, expectedReleaseDay == 0 ? 30 : expectedReleaseDay, 0, 0);
		return release;
	}

	public boolean isOut() {
		DateTime now = new DateTime();
		DateTime release = getReleaseDate().plusDays(1);
		Days d = Days.daysBetween(now, release);
		int days = d.getDays();
		return days <= 0;
	}

	@Override
	public String toString() {
		return name;
	}

}