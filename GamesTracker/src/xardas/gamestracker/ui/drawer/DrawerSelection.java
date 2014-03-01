package xardas.gamestracker.ui.drawer;

public enum DrawerSelection {
	TRACKED(0), THIS_MONTH(1), NEXT_MONTH(2), YEAR(3), SEARCH(4), SETTINGS(5);

	private int value;

	private DrawerSelection(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}