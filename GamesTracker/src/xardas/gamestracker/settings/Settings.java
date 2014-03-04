package xardas.gamestracker.settings;

import java.util.ArrayList;
import java.util.List;

public class Settings {

	private boolean notify;
	private int duration;
	private List<Integer> autoExpand;

	public Settings() {
		notify = true;
		duration = 7;
		autoExpand = new ArrayList<Integer>();
		autoExpand.add(0);
		autoExpand.add(1);
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

	public List<Integer> getAutoExpand() {
		return autoExpand;
	}

	public void setAutoExpand(List<Integer> autoExpand) {
		this.autoExpand = autoExpand;
	}
}
