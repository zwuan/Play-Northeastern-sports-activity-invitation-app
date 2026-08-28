package application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Each group names the defect it guards against, so a failure points back at
 * what broke rather than just at a line number.
 */
class InvitationTest {

    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();

    private static Invitation invitation() {
        return new Invitation("Alice", TOMORROW, 9, 0, 10, 0, 4, "Basketball",
                Location.CARTER_PLAYGROUND, Gender.ALL_GENDER);
    }

    @Nested
    @DisplayName("editing is validated exactly like creating")
    class EditValidation {

        // The edit path used to bypass every constructor check, so "99:-5" and a
        // player count of -3 were both storable through the UI.
        @ParameterizedTest(name = "rejects {0}")
        @CsvSource({
            "hour 99,              99,  0, 10,  0,  4",
            "negative minute,       9, -5, 10,  0,  4",
            "minute 60,             9, 60, 10,  0,  4",
            "end hour 24,           9,  0, 24,  0,  4",
            "end equal to start,    9,  0,  9,  0,  4",
            "end before start,     18,  0,  9,  0,  4",
            "zero players,          9,  0, 10,  0,  0",
            "negative players,      9,  0, 10,  0, -3",
        })
        void rejectsImpossibleValues(String description, int startH, int startM, int endH, int endM, int count) {
            Invitation inv = invitation();

            assertThrows(IllegalArgumentException.class, () -> inv.update("Alice", TOMORROW,
                    startH, startM, endH, endM, count, "Soccer", Location.CARTER_PLAYGROUND, Gender.MALE));

            assertThrows(IllegalArgumentException.class, () -> new Invitation("Alice", TOMORROW,
                    startH, startM, endH, endM, count, "Soccer", Location.CARTER_PLAYGROUND, Gender.MALE),
                    "the constructor must reject whatever update() rejects");
        }

        @Test
        void leavesTheInvitationUntouchedWhenAnEditIsRejected() {
            Invitation inv = invitation();

            assertThrows(IllegalArgumentException.class, () -> inv.update("Alice", TOMORROW,
                    99, 0, 10, 0, 4, "Soccer", Location.CABOT_CENTER, Gender.MALE));

            assertAll(
                () -> assertEquals("09:00 ~ 10:00", inv.getTimeSlot()),
                () -> assertEquals(4, inv.getCount()),
                () -> assertEquals(Location.CARTER_PLAYGROUND, inv.getLocation()));
        }

        // Invitation had no organizer setter, so renaming reverted on the next
        // table rebuild. update() replaces every editable field, including this.
        @Test
        void appliesAnOrganizerRename() {
            Invitation inv = invitation();

            inv.update("Alice Chen", TOMORROW, 8, 0, 9, 30, 6, "Soccer",
                    Location.CARTER_PLAYGROUND, Gender.MALE);

            assertEquals("Alice Chen", inv.getOrganizer());
            assertEquals("Alice Chen", Activity.of(inv).getOrganizer(),
                    "the rename has to survive the table rebuild");
        }
    }

    @Nested
    @DisplayName("blank and null input is rejected, never dereferenced")
    class NullHandling {

        // A null location used to reach switch (location) and throw NPE.
        @Test
        void rejectsNullsWithAMessageRatherThanAnNpe() {
            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("O", TOMORROW,
                        9, 0, 10, 0, 4, "Golf", null, Gender.MALE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("O", TOMORROW,
                        9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("O", TOMORROW,
                        9, 0, 10, 0, 4, null, Location.CABOT_CENTER, Gender.MALE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation(null, TOMORROW,
                        9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, Gender.MALE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("O", null,
                        9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, Gender.MALE)));
        }

        @Test
        void rejectsBlankText() {
            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("   ", TOMORROW,
                        9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, Gender.MALE)),
                () -> assertThrows(IllegalArgumentException.class, () -> new Invitation("O", TOMORROW,
                        9, 0, 10, 0, 4, "   ", Location.CABOT_CENTER, Gender.MALE)));
        }

        @Test
        void rejectsADateThatIsNotIsoFormat() {
            assertThrows(IllegalArgumentException.class, () -> new Invitation("O", "not-a-date",
                    9, 0, 10, 0, 4, "Golf", Location.CABOT_CENTER, Gender.MALE));
        }

        @Test
        void trimsTextOnTheWayIn() {
            Invitation inv = new Invitation("  Alice  ", TOMORROW, 9, 0, 10, 0, 4, "  Tennis  ",
                    Location.CABOT_CENTER, Gender.MALE);

            assertEquals("Alice", inv.getOrganizer());
            assertEquals("Tennis", inv.getSport());
        }
    }

    @Nested
    @DisplayName("the PIN tracks the current location and gender")
    class PinDerivation {

        // The PIN encodes location and gender but used to be frozen at
        // construction, so an edit left it describing the old values.
        @Test
        void reEncodesAfterALocationAndGenderEdit() {
            Invitation inv = new Invitation("Zoe", TOMORROW, 9, 0, 10, 0, 4, "Yoga",
                    Location.MARINO_RECREATION_CENTER, Gender.FEMALE);
            String before = inv.getPin();
            assertTrue(before.startsWith("MR") && before.endsWith("F"), before);

            inv.update("Zoe", TOMORROW, 9, 0, 10, 0, 4, "Yoga", Location.CABOT_CENTER, Gender.MALE);

            String after = inv.getPin();
            assertTrue(after.startsWith("CC") && after.endsWith("M"), after);
            assertEquals(before.substring(2, 5), after.substring(2, 5),
                    "only the encoded halves change; the random middle is kept");
        }

        @Test
        void isSixCharactersOfLocationDigitsGender() {
            Invitation inv = invitation();
            assertTrue(inv.getPin().matches("[A-Z]{2}\\d{3}[A-Z]"), inv.getPin());
        }
    }

    @Nested
    @DisplayName("capacity")
    class Capacity {

        // The count could be dropped below joinedCount, showing "5 / 2".
        @Test
        void cannotDropBelowThePlayersAlreadyJoined() {
            Invitation inv = invitation();
            for (int i = 0; i < 4; i++) {
                inv.incrementJoined();
            }

            assertThrows(IllegalArgumentException.class, () -> inv.update("Alice", TOMORROW,
                    9, 0, 10, 0, 2, "Basketball", Location.CARTER_PLAYGROUND, Gender.ALL_GENDER));
            assertEquals(4, inv.getCount(), "the rejected edit must not have applied");
        }

        @Test
        void allowsACountEqualToThePlayersAlreadyJoined() {
            Invitation inv = invitation();
            for (int i = 0; i < 4; i++) {
                inv.incrementJoined();
            }

            inv.update("Alice", TOMORROW, 9, 0, 10, 0, 4, "Basketball",
                    Location.CARTER_PLAYGROUND, Gender.ALL_GENDER);

            assertEquals(4, inv.getCount());
        }

        @Test
        void refusesAJoinOnceFull() {
            Invitation inv = invitation();
            assertFalse(inv.isFull());

            for (int i = 0; i < 4; i++) {
                inv.incrementJoined();
            }

            assertTrue(inv.isFull());
            assertThrows(IllegalStateException.class, inv::incrementJoined);
            assertEquals(4, inv.getJoinedCount());
        }
    }
}
