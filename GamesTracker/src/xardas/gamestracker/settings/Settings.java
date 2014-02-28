package xardas.gamestracker.settings;

public class Settings {

	private boolean notify;
	private int duration;

	public Settings() {
		notify = true;
		duration = 7;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public int getDuration() {
		return duration;
	}

	public boolean isNotify() {
		return notify;
	}
}
