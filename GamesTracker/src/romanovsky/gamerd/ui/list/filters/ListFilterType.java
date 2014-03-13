package romanovsky.gamerd.ui.list.filters;

public enum ListFilterType {
	PLATFORMS(0), GENRES(1);

	private int value;

	private ListFilterType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}