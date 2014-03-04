package xardas.gamestracker.giantbomb.api;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable {
	private String name;
	private long id;
	private long dateLastUpdated;
	private int expectedReleaseDay;
	private int expectedReleaseMonth;
	private int expectedReleaseYear;
	private int expectedReleaseQuarter;
	private List<String> platforms;
	private String iconURL;
	private String siteDetailURL;
	private boolean notify;
	private String description;
	private boolean tracked;

	public Game() {
		platforms = new ArrayList<String>();
	}

	public Game(Parcel in) {
		this();
		name = in.readString();
		id = in.readLong();
		dateLastUpdated = in.readLong();
		expectedReleaseDay = in.readInt();
		expectedReleaseMonth = in.readInt();
		expectedReleaseYear = in.readInt();
		expectedReleaseQuarter = in.readInt();
		in.readStringList(platforms);
		iconURL = in.readString();
		siteDetailURL = in.readString();
		notify = in.readInt() == 1;
		description = in.readString();
		tracked = in.readInt() == 1;
	}

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

	public String getSiteDetailURL() {
		return siteDetailURL;
	}

	public void setSiteDetailURL(String siteDetailURL) {
		this.siteDetailURL = siteDetailURL;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DateTime getReleaseDate() {
		DateTime release = new DateTime(expectedReleaseYear, expectedReleaseMonth == 0 ? 12 : expectedReleaseMonth, expectedReleaseDay == 0 ? 30 : expectedReleaseDay, 0, 0);
		return release;
	}

	public boolean isTracked() {
		return tracked;
	}

	public void setTracked(boolean tracked) {
		this.tracked = tracked;
	}

	public int isOutFor() {
		DateTime now = new DateTime();
		DateTime release = getReleaseDate().plusDays(1);
		Days d = Days.daysBetween(now, release);
		int days = d.getDays();
		return days;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		boolean equals = false;
		if (o instanceof Game) {
			equals = id == ((Game) o).id;
		}
		return equals;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(id);
		dest.writeLong(dateLastUpdated);
		dest.writeInt(expectedReleaseDay);
		dest.writeInt(expectedReleaseMonth);
		dest.writeInt(expectedReleaseYear);
		dest.writeInt(expectedReleaseQuarter);
		dest.writeStringList(platforms);
		dest.writeString(iconURL);
		dest.writeString(siteDetailURL);
		dest.writeInt(notify ? 1 : 0);
		dest.writeString(description);
		dest.writeInt(tracked ? 1 : 0);
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Game createFromParcel(Parcel in) {
			return new Game(in);
		}

		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
}