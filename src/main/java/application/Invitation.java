package application;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Random;

/**
 * One sports activity invitation.
 *
 * Creating and editing an invitation both run through {@link #validate}, so an
 * edit can never store a time range, player count or location that the create
 * form would have refused. That is why there are no per-field setters: a time
 * range is only valid as a whole, so it can only be replaced as a whole, via
 * {@link #update}.
 *
 * The PIN is derived from the current location and gender rather than stored,
 * so it cannot drift out of sync with them. Only the random middle is state,
 * and {@link InvitationManager} owns it because uniqueness needs the whole list.
 */
public class Invitation {

	/** How many distinct PINs exist for one location/gender pair. */
	static final int PIN_RANDOM_BOUND = 1000;

	private static final Random RANDOM = new Random();

	private String name;
	private String date;
	private int startH;
	private int startM;
	private int endH;
	private int endM;
	private int count;
	private String sport;
	private Location location;
	private Gender gender;
	private int pinSuffix;
	private int joinedCount = 0;

	public Invitation(String name, String date, int startH, int startM, int endH, int endM, int count, String sport,
			Location location, Gender gender) {

		validate(name, date, startH, startM, endH, endM, count, sport, location, gender);
		assign(name, date, startH, startM, endH, endM, count, sport, location, gender);
		this.pinSuffix = RANDOM.nextInt(PIN_RANDOM_BOUND);
	}

	/**
	 * Replaces every editable field in one shot, under the same validation the
	 * constructor uses. Prefer {@link InvitationManager#updateInvitation} over
	 * calling this directly: changing the location or gender changes the PIN,
	 * and only the manager can keep PINs unique.
	 */
	public void update(String name, String date, int startH, int startM, int endH, int endM, int count, String sport,
			Location location, Gender gender) {

		validate(name, date, startH, startM, endH, endM, count, sport, location, gender);

		if (count < joinedCount) {
			throw new IllegalArgumentException(
					"Player count cannot be lower than the " + joinedCount + " player(s) already joined.");
		}

		assign(name, date, startH, startM, endH, endM, count, sport, location, gender);
	}

	private static void validate(String name, String date, int startH, int startM, int endH, int endM, int count,
			String sport, Location location, Gender gender) {

		requireText(name, "organizer name");
		requireText(sport, "sport name");
		requireText(date, "date");

		try {
			LocalDate.parse(date.trim());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Date must be in YYYY-MM-DD form.");
		}

		if (location == null) {
			throw new IllegalArgumentException("Please select a location.");
		}

		if (gender == null) {
			throw new IllegalArgumentException("Please select a gender.");
		}

		if (!isValidTime(startH, startM)) {
			throw new IllegalArgumentException("Start time must be between 00:00 and 23:59.");
		}

		if (!isValidTime(endH, endM)) {
			throw new IllegalArgumentException("End time must be between 00:00 and 23:59.");
		}

		if (endH * 60 + endM <= startH * 60 + startM) {
			throw new IllegalArgumentException("End time must be after start time.");
		}

		if (count <= 0) {
			throw new IllegalArgumentException("Player count must be at least 1.");
		}
	}

	private void assign(String name, String date, int startH, int startM, int endH, int endM, int count, String sport,
			Location location, Gender gender) {

		this.name = name.trim();
		this.date = date.trim();
		this.startH = startH;
		this.startM = startM;
		this.endH = endH;
		this.endM = endM;
		this.count = count;
		this.sport = sport.trim();
		this.location = location;
		this.gender = gender;
	}

	private static void requireText(String value, String field) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Please enter the " + field + ".");
		}
	}

	private static boolean isValidTime(int h, int m) {
		return h >= 0 && h <= 23 && m >= 0 && m <= 59;
	}

	/** Location code + three random digits + gender code, e.g. MR168F. */
	public String getPin() {
		return buildPin(pinSuffix);
	}

	/** What the PIN would be with this random middle. Used to search for a free one. */
	String buildPin(int suffix) {
		return location.getCode() + String.format("%03d", suffix) + gender.getCode();
	}

	int getPinSuffix() {
		return pinSuffix;
	}

	void setPinSuffix(int pinSuffix) {
		this.pinSuffix = pinSuffix;
	}

	public String getOrganizer() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public int getStartH() {
		return startH;
	}

	public int getStartM() {
		return startM;
	}

	public int getEndH() {
		return endH;
	}

	public int getEndM() {
		return endM;
	}

	public int getCount() {
		return count;
	}

	public String getSport() {
		return sport;
	}

	public Location getLocation() {
		return location;
	}

	public Gender getGender() {
		return gender;
	}

	public String getTimeSlot() {
		return String.format("%02d:%02d ~ %02d:%02d", startH, startM, endH, endM);
	}

	public int getJoinedCount() {
		return joinedCount;
	}

	public boolean isFull() {
		return joinedCount >= count;
	}

	public void incrementJoined() {
		if (isFull()) {
			throw new IllegalStateException("This activity is already full.");
		}
		joinedCount++;
	}
}
