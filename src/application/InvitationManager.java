package application;

import java.util.ArrayList;
import java.util.Stack;

public class InvitationManager {
    private ArrayList<Invitation> invitationList;
    private Stack<String> recentActivityStack;

    public InvitationManager() {
        invitationList = new ArrayList<>();
        recentActivityStack = new Stack<>();
    }

    public void addInvitation(Invitation invitation) {
        if (invitation != null) {
            invitationList.add(invitation);
            recentActivityStack.push(formatRecentActivity(invitation));
        }
    }

    public ArrayList<Invitation> getInvitationList() {
        return invitationList;
    }

    public Invitation findByPin(String pin) {
        if (pin == null) {
            return null;
        }

        for (Invitation invitation : invitationList) {
            if (invitation.getPin().equalsIgnoreCase(pin)) {
                return invitation;
            }
        }
        return null;
    }

    public boolean removeByPin(String pin) {
        Invitation invitation = findByPin(pin);
        if (invitation != null) {
            invitationList.remove(invitation);
            return true;
        }
        return false;
    }

    public int getSize() {
        return invitationList.size();
    }

    public boolean isEmpty() {
        return invitationList.isEmpty();
    }

    private String formatRecentActivity(Invitation invitation) {
        return "Latest Activity:" + " | "+ invitation.getSport()  + " | "+ invitation.getLocation() + " | "+ invitation.getTimeSlot();
    }

    public String getLatestActivityText() {
        if (recentActivityStack.isEmpty()) {
            return "No activity has been added yet.";
        }
        return recentActivityStack.peek();
    }
}