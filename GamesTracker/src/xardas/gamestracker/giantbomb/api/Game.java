package xardas.gamestracker.giantbomb.api;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

	public Date getReleaseDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(expectedReleaseYear, expectedReleaseMonth == 0 ? 12 : expectedReleaseMonth - 1, expectedReleaseDay == 0 ? 31 : expectedReleaseDay);
		return calendar.getTime();
	}

	@Override
	public String toString() {
		return name;
	}

}