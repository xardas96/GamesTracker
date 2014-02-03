package xardas.gamestracker.giantbomb.api;

import java.util.List;

public class Game {
	private String name;
	private long id;
	private long dateLastUpdated;
	private int expectedReleaseDay;
	private int expectedReleaseMonth;
	private int expectedReleaseYear;

	private List<String> platforms;

	private String expectedReleaseQuarter;

	private String iconURL;
	private String smallURL;

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

	public String getExpectedReleaseQuarter() {
		return expectedReleaseQuarter;
	}

	public void setExpectedReleaseQuarter(String expectedReleaseQuarter) {
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

	@Override
	public String toString() {
		return name;
	}

}