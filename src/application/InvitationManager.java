package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

/**
 * Owns every invitation in the app, and owns PIN assignment with it: a PIN is
 * the only handle the UI has on an invitation, so it has to be unique, and only
 * this class can see the whole list.
 */
public class InvitationManager {

    private static final Random RANDOM = new Random();

    // Stores all invitations created by users
    private final ArrayList<Invitation> invitationList;

    // Recently added activities (LIFO: last added = top of stack). Holds the
    // invitations themselves rather than pre-formatted text, so the home screen
    // cannot keep showing an activity that has since been edited or removed.
    private final Stack<Invitation> recentActivityStack;

    public InvitationManager() {
        invitationList = new ArrayList<>();
        recentActivityStack = new Stack<>();
    }

    /**
     * Adds an invitation and gives it a PIN no other invitation holds. An
     * invitation picks a random PIN for itself on construction; this is where
     * that pick is checked against everything else.
     */
    public void addInvitation(Invitation invitation) {
        if (invitation == null) {
            return;
        }

        ensureUniquePin(invitation);
        invitationList.add(invitation);
        recentActivityStack.push(invitation);
    }

    /**
     * Applies an edit through {@link Invitation#update}, so it faces the same
     * validation as creating one, then repairs the PIN: the PIN encodes location
     * and gender, so changing either can land on a PIN already in use.
     *
     * @return the PIN after the edit, which may differ from the one before it
     */
    public String updateInvitation(Invitation invitation, String name, String date, int startH, int startM, int endH,
            int endM, int count, String sport, Location location, Gender gender) {

        if (invitation == null) {
            throw new IllegalArgumentException("No activity selected.");
        }

        invitation.update(name, date, startH, startM, endH, endM, count, sport, location, gender);
        ensureUniquePin(invitation);
        return invitation.getPin();
    }

    public ArrayList<Invitation> getInvitationList() {
        return invitationList;
    }

    /** Finds an invitation by its PIN. Case does not matter. */
    public Invitation findByPin(String pin) {
        if (pin == null) {
            return null;
        }

        String trimmed = pin.trim();
        for (Invitation invitation : invitationList) {
            if (invitation.getPin().equalsIgnoreCase(trimmed)) {
                return invitation;
            }
        }
        return null;
    }

    /** Removes an invitation by its PIN, including from the recent-activity stack. */
    public boolean removeByPin(String pin) {
        Invitation invitation = findByPin(pin);
        if (invitation == null) {
            return false;
        }

        invitationList.remove(invitation);
        recentActivityStack.removeIf(entry -> entry == invitation);
        return true;
    }

    public int getSize() {
        return invitationList.size();
    }

    public boolean isEmpty() {
        return invitationList.isEmpty();
    }

    public String getLatestActivityText() {
        if (recentActivityStack.isEmpty()) {
            return "No activity has been added yet.";
        }
        return formatRecentActivity(recentActivityStack.peek());
    }

    // Formatted on read, so an edit to the newest activity shows up immediately.
    private String formatRecentActivity(Invitation invitation) {
        return "Latest Activity:" + " | " + invitation.getSport() + " | " + invitation.getLocation().getDisplayName()
                + " | " + invitation.getTimeSlot();
    }

    /**
     * Leaves the PIN alone if it is already free, otherwise walks the 1000 PINs
     * available for this location/gender pair in random order and takes the
     * first unused one. Sweeping the whole range instead of retrying at random
     * means a near-full range still terminates.
     */
    private void ensureUniquePin(Invitation invitation) {
        Set<String> taken = new HashSet<>();
        for (Invitation other : invitationList) {
            if (other != invitation) {
                taken.add(other.getPin().toUpperCase());
            }
        }

        if (!taken.contains(invitation.getPin().toUpperCase())) {
            return;
        }

        List<Integer> suffixes = new ArrayList<>(Invitation.PIN_RANDOM_BOUND);
        for (int suffix = 0; suffix < Invitation.PIN_RANDOM_BOUND; suffix++) {
            suffixes.add(suffix);
        }
        Collections.shuffle(suffixes, RANDOM);

        for (int suffix : suffixes) {
            if (!taken.contains(invitation.buildPin(suffix).toUpperCase())) {
                invitation.setPinSuffix(suffix);
                return;
            }
        }

        throw new IllegalStateException("No PIN left for " + invitation.getLocation().getDisplayName() + " / "
                + invitation.getGender().getDisplayName() + ".");
    }
}
