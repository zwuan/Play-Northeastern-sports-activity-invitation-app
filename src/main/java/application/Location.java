package application;

/**
 * The bookable locations around campus, each with the two-letter code that
 * opens every PIN for an activity held there.
 *
 * The display name, the PIN code and the list offered by the create form used
 * to live in three different files, which is how the map ended up showing
 * courts that could not be booked. They all come from here now.
 */
public enum Location {

	MARINO_RECREATION_CENTER("Marino Recreation Center", "MR"),
	CABOT_CENTER("Cabot Center", "CC"),
	CARTER_PLAYGROUND("Carter Playground", "CP"),
	SQUASH_BUSTERS("SquashBusters", "SB"),
	ROXBURY_YMCA("Roxbury YMCA", "RY"),
	FENWAY_COURT("Fenway Court", "FC"),
	TITUS_SPARROW_PARK("Titus Sparrow Park", "TS");

	private final String displayName;
	private final String code;

	Location(String displayName, String code) {
		this.displayName = displayName;
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** The two leading characters of a PIN for this location. */
	public String getCode() {
		return code;
	}

	/** Returns the location with this display name, or null if there is none. */
	public static Location fromDisplayName(String displayName) {
		if (displayName == null) {
			return null;
		}

		String trimmed = displayName.trim();
		for (Location location : values()) {
			if (location.displayName.equalsIgnoreCase(trimmed)) {
				return location;
			}
		}
		return null;
	}

	/** Used by the ComboBox and by the table, so it has to be the display name. */
	@Override
	public String toString() {
		return displayName;
	}
}
