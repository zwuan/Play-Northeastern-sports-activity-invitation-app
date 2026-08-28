package application;

/**
 * The gender restriction an organizer can put on an activity, with the single
 * character that closes every PIN.
 */
public enum Gender {

	MALE("Male", "M"),
	FEMALE("Female", "F"),
	ALL_GENDER("All Gender", "A");

	private final String displayName;
	private final String code;

	Gender(String displayName, String code) {
		this.displayName = displayName;
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** The trailing character of a PIN for this restriction. */
	public String getCode() {
		return code;
	}

	/** Returns the restriction with this display name, or null if there is none. */
	public static Gender fromDisplayName(String displayName) {
		if (displayName == null) {
			return null;
		}

		String trimmed = displayName.trim();
		for (Gender gender : values()) {
			if (gender.displayName.equalsIgnoreCase(trimmed)) {
				return gender;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
