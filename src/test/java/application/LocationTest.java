package application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Location and Gender exist because the display name, the PIN code and the
 * create form's dropdown list used to be hard-coded in three separate files,
 * which is how the map ended up offering courts that could not be booked.
 */
class LocationTest {

    // Every court drawn as a polygon in Main.fxml. If a polygon is added there,
    // add it here too -- this test is what keeps the map and the form in step.
    @ParameterizedTest
    @ValueSource(strings = {
        "Carter Playground",
        "Marino Recreation Center",
        "Cabot Center",
        "Fenway Court",
        "Titus Sparrow Park",
    })
    void everyCourtOnTheMapIsBookable(String displayName) {
        assertNotNull(Location.fromDisplayName(displayName),
                displayName + " is on the map but the create form cannot offer it");
    }

    @Test
    void pinCodesAreDistinct() {
        Set<String> codes = new HashSet<>();
        for (Location location : Location.values()) {
            codes.add(location.getCode());
        }
        assertEquals(Location.values().length, codes.size(), codes.toString());
    }

    @ParameterizedTest
    @EnumSource(Location.class)
    void codeIsTwoUppercaseLetters(Location location) {
        assertEquals(2, location.getCode().length(), location.name());
        assertEquals(location.getCode().toUpperCase(), location.getCode(), location.name());
    }

    @ParameterizedTest
    @EnumSource(Location.class)
    void roundTripsThroughItsDisplayName(Location location) {
        assertSame(location, Location.fromDisplayName(location.getDisplayName()));
        assertSame(location, Location.fromDisplayName("  " + location.getDisplayName() + "  "));
        assertEquals(location.getDisplayName(), location.toString(),
                "the ComboBox and the table render this, so it has to be the display name");
    }

    @ParameterizedTest
    @EnumSource(Gender.class)
    void genderRoundTripsAndHasAOneLetterCode(Gender gender) {
        assertSame(gender, Gender.fromDisplayName(gender.getDisplayName()));
        assertEquals(1, gender.getCode().length(), gender.name());
        assertEquals(gender.getDisplayName(), gender.toString());
    }

    @Test
    void unknownNamesResolveToNullRatherThanASilentFallback() {
        assertNull(Location.fromDisplayName("Fenway Park"));
        assertNull(Location.fromDisplayName(null));
        assertNull(Gender.fromDisplayName("Other"));
        assertNull(Gender.fromDisplayName(null));
    }
}
