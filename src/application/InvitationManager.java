package application;

import java.util.ArrayList;

public class InvitationManager {
    private ArrayList<Invitation> invitationList;

    public InvitationManager() {
        invitationList = new ArrayList<>();
    }

    public void addInvitation(Invitation invitation) {
        if (invitation != null) {
            invitationList.add(invitation);
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
}
