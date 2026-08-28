package application;

/**
 * One row of the activity table: a read-only snapshot of an {@link Invitation},
 * built fresh every time the table is loaded.
 *
 * It is immutable on purpose. It used to have setters, and the edit form called
 * them alongside updating the invitation — but the table rebuilds all of its
 * rows from the invitations, so those writes were thrown away and any field
 * without a matching invitation setter silently reverted. The invitation is the
 * only thing worth writing to; this is just what the table draws.
 */
public class Activity {

	private final String activityName;
	private final String organizer;
	private final String date;
	private final String location;
	private final String timeSlot;
	private final String gender;
	private final String status;
	private final String pin;

	public Activity(String activityName, String organizer, String date, String location, String status, String pin,
			String timeSlot, String gender) {

		this.activityName = activityName;
		this.organizer = organizer;
		this.date = date;
		this.location = location;
		this.status = status;
		this.pin = pin;
		this.timeSlot = timeSlot;
		this.gender = gender;
	}

	/** Builds the table row for an invitation. */
	public static Activity of(Invitation invitation) {
		return new Activity(invitation.getSport(), invitation.getOrganizer(), invitation.getDate(),
				invitation.getLocation().getDisplayName(),
				invitation.getJoinedCount() + " / " + invitation.getCount(), invitation.getPin(),
				invitation.getTimeSlot(), invitation.getGender().getDisplayName());
	}

	public String getActivityName() {
		return activityName;
	}

	public String getOrganizer() {
		return organizer;
	}

	public String getDate() {
		return date;
	}

	public String getLocation() {
		return location;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public String getGender() {
		return gender;
	}

	public String getStatus() {
		return status;
	}

	/** The handle back to the underlying invitation. */
	public String getPin() {
		return pin;
	}
}
