package application;

import java.util.ArrayList;
import java.util.Stack;

public class InvitationManager {
    private ArrayList<Invitation> invitationList;// Stores all invitations created by users
    private Stack<String> recentActivityStack;// Stack to track recently added activities (LIFO: last added = top of stack)
    
    //initializes the invitation list and recent activity stack.
    public InvitationManager() {
        invitationList = new ArrayList<>();
        recentActivityStack = new Stack<>();
    }
    
    // add a new invitation to the list and pushes a summary to the recent activity stack.
    public void addInvitation(Invitation invitation) {
        if (invitation != null) {
            invitationList.add(invitation);
            recentActivityStack.push(formatRecentActivity(invitation));
        }
    }

    public ArrayList<Invitation> getInvitationList() {
        return invitationList;
    }
    
    //Finds an invitation from the list by its PIN.
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
    
    //Removes an invitation from the list by its PIN.
    public boolean removeByPin(String pin) {
        Invitation invitation = findByPin(pin);
        if (invitation != null) {
            invitationList.remove(invitation);
            return true;
        }
        return false;
    }
    
    //total number of invitations currently in the list.
    public int getSize() {
        return invitationList.size();
    }

    public boolean isEmpty() {
        return invitationList.isEmpty();
    }
    //Display RecentActivity in String
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