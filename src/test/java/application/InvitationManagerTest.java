package application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvitationManagerTest {

    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();

    private final InvitationManager manager = new InvitationManager();

    private Invitation add(String organizer, Location location, Gender gender) {
        Invitation invitation = new Invitation(organizer, TOMORROW, 9, 0, 10, 0, 4, "Basketball",
                location, gender);
        manager.addInvitation(invitation);
        return invitation;
    }

    @Nested
    @DisplayName("PINs are unique across every invitation")
    class PinUniqueness {

        // The PIN used to be drawn from 1000 values per location/gender pair with
        // no collision check, and findByPin returns the first match -- so joining
        // or editing could act on the wrong activity. 4.4% chance with only 10
        // activities in one pair, 55% with 40.
        @Test
        void everyPinInOneLocationGenderPairIsDistinct() {
            Set<String> pins = new HashSet<>();
            for (int i = 0; i < Invitation.PIN_RANDOM_BOUND; i++) {
                pins.add(add("Org", Location.MARINO_RECREATION_CENTER, Gender.FEMALE).getPin());
            }

            assertEquals(Invitation.PIN_RANDOM_BOUND, pins.size());
        }

        @Test
        void reportsExhaustionRatherThanReusingAPin() {
            for (int i = 0; i < Invitation.PIN_RANDOM_BOUND; i++) {
                add("Org", Location.MARINO_RECREATION_CENTER, Gender.FEMALE);
            }

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> add("Org", Location.MARINO_RECREATION_CENTER, Gender.FEMALE));
            assertTrue(thrown.getMessage().contains("Marino Recreation Center"), thrown.getMessage());
        }

        // Editing the location moves the invitation into another PIN range, where
        // its random middle may already be taken.
        @Test
        void anEditIntoAnOccupiedRangeKeepsPinsDistinct() {
            Invitation stay = add("Stay", Location.CABOT_CENTER, Gender.MALE);
            Invitation move = add("Move", Location.ROXBURY_YMCA, Gender.MALE);

            String moved = manager.updateInvitation(move, "Move", TOMORROW, 9, 0, 10, 0, 4,
                    "Squash", Location.CABOT_CENTER, Gender.MALE);

            assertNotEquals(stay.getPin().toUpperCase(), moved.toUpperCase());
            assertSame(stay, manager.findByPin(stay.getPin()));
            assertSame(move, manager.findByPin(moved));
        }
    }

    @Nested
    @DisplayName("lookup")
    class Lookup {

        @Test
        void findsByPinIgnoringCaseAndSurroundingSpace() {
            Invitation inv = add("Alice", Location.CABOT_CENTER, Gender.MALE);

            assertAll(
                () -> assertSame(inv, manager.findByPin(inv.getPin())),
                () -> assertSame(inv, manager.findByPin(inv.getPin().toLowerCase())),
                () -> assertSame(inv, manager.findByPin("  " + inv.getPin() + "  ")));
        }

        @Test
        void returnsNullForAnUnknownOrNullPin() {
            add("Alice", Location.CABOT_CENTER, Gender.MALE);

            assertNull(manager.findByPin("ZZ999Z"));
            assertNull(manager.findByPin(null));
        }

        @Test
        void stopsResolvingThePinAnEditReplaced() {
            Invitation inv = add("Zoe", Location.MARINO_RECREATION_CENTER, Gender.FEMALE);
            String before = inv.getPin();

            String after = manager.updateInvitation(inv, "Zoe", TOMORROW, 9, 0, 10, 0, 4,
                    "Yoga", Location.CABOT_CENTER, Gender.MALE);

            assertNull(manager.findByPin(before));
            assertSame(inv, manager.findByPin(after));
        }
    }

    @Nested
    @DisplayName("the latest-activity text follows the invitation it points at")
    class RecentActivity {

        // The stack held pre-formatted strings, so the home screen kept showing
        // activities that had since been edited or removed.
        @Test
        void reflectsAnEdit() {
            Invitation inv = add("Zoe", Location.ROXBURY_YMCA, Gender.FEMALE);

            manager.updateInvitation(inv, "Zoe", TOMORROW, 20, 0, 21, 0, 4, "Volleyball",
                    Location.CABOT_CENTER, Gender.FEMALE);

            String latest = manager.getLatestActivityText();
            assertAll(
                () -> assertTrue(latest.contains("Volleyball"), latest),
                () -> assertTrue(latest.contains("Cabot Center"), latest),
                () -> assertTrue(latest.contains("20:00 ~ 21:00"), latest));
        }

        @Test
        void clearsWhenTheOnlyInvitationIsRemoved() {
            Invitation inv = add("Zoe", Location.ROXBURY_YMCA, Gender.FEMALE);

            assertTrue(manager.removeByPin(inv.getPin()));

            assertEquals(0, manager.getSize());
            assertTrue(manager.isEmpty());
            assertEquals("No activity has been added yet.", manager.getLatestActivityText());
        }

        @Test
        void fallsBackToThePreviousActivityWhenTheNewestIsRemoved() {
            Invitation first = add("First", Location.CABOT_CENTER, Gender.MALE);
            Invitation newest = add("Newest", Location.ROXBURY_YMCA, Gender.FEMALE);
            assertTrue(manager.getLatestActivityText().contains("Roxbury YMCA"));

            manager.removeByPin(newest.getPin());

            assertTrue(manager.getLatestActivityText().contains("Cabot Center"),
                    manager.getLatestActivityText());
            assertSame(first, manager.findByPin(first.getPin()));
        }

        @Test
        void saysSoWhenNothingHasBeenAdded() {
            assertEquals("No activity has been added yet.", manager.getLatestActivityText());
        }
    }

    @Nested
    @DisplayName("misc")
    class Misc {

        @Test
        void ignoresANullInvitation() {
            manager.addInvitation(null);
            assertTrue(manager.isEmpty());
        }

        @Test
        void refusesToUpdateANullInvitation() {
            assertThrows(IllegalArgumentException.class, () -> manager.updateInvitation(null,
                    "Alice", TOMORROW, 9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, Gender.MALE));
        }

        @Test
        void reportsFailureWhenRemovingAnUnknownPin() {
            assertFalse(manager.removeByPin("ZZ999Z"));
        }
    }
}
