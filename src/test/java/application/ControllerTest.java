package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the controllers on the JavaFX application thread.
 *
 * Reflection is used to reach the @FXML fields and the private @FXML handlers.
 * That is a smell rather than a preference: handleSave() ends in
 * Alert.showAndWait(), which blocks the FX thread, so the form validation
 * cannot be exercised here at all. Extracting that validation into a function
 * that returns a result instead of showing a dialog would make it testable and
 * let this class drop the reflection.
 */
class ControllerTest {

    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();

    @SuppressWarnings("unchecked")
    private static <T> T field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void invokeHandler(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static Object load(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        loader.load();
        return loader.getController();
    }

    private static InvitationManager threeActivitiesOfThree() {
        InvitationManager manager = new InvitationManager();
        for (String organizer : new String[] {"Alice", "Bob", "Carol"}) {
            manager.addInvitation(new Invitation(organizer, TOMORROW, 9, 0, 10, 0, 3, "Basketball",
                    Location.CABOT_CENTER, Gender.ALL_GENDER));
        }
        return manager;
    }

    @Test
    @DisplayName("joining keeps the row selected, because the table rebuilds every row")
    void joinKeepsSelection() throws Exception {
        FxToolkit.onFxThread(() -> {
            InvitationManager manager = threeActivitiesOfThree();
            ActivityListController controller = (ActivityListController) load("ActivityList.fxml");
            controller.setManager(manager);

            TableView<Activity> table = field(controller, "activityTable");
            Label message = field(controller, "messageLabel");
            assertEquals(3, table.getItems().size());

            table.getSelectionModel().select(1);
            String pinBefore = table.getSelectionModel().getSelectedItem().getPin();
            invokeHandler(controller, "handleJoin");

            Activity after = table.getSelectionModel().getSelectedItem();
            assertNotNull(after, "the selection was dropped by the table rebuild");
            assertEquals(pinBefore, after.getPin());
            assertEquals("1 / 3", after.getStatus());
            assertTrue(message.getText().startsWith("Joined: Basketball"), message.getText());
        });
    }

    @Test
    @DisplayName("a full activity refuses another join instead of throwing")
    void joinStopsAtCapacity() throws Exception {
        FxToolkit.onFxThread(() -> {
            InvitationManager manager = threeActivitiesOfThree();
            ActivityListController controller = (ActivityListController) load("ActivityList.fxml");
            controller.setManager(manager);

            TableView<Activity> table = field(controller, "activityTable");
            Label message = field(controller, "messageLabel");

            table.getSelectionModel().select(0);
            for (int i = 0; i < 3; i++) {
                invokeHandler(controller, "handleJoin");
            }
            assertEquals("3 / 3", table.getSelectionModel().getSelectedItem().getStatus());

            invokeHandler(controller, "handleJoin");
            assertEquals("This activity is already full.", message.getText());
            assertEquals("3 / 3", table.getSelectionModel().getSelectedItem().getStatus());
        });
    }

    @Test
    @DisplayName("joining under an active filter keeps both the filter and the selection")
    void joinPreservesTheFilter() throws Exception {
        FxToolkit.onFxThread(() -> {
            InvitationManager manager = threeActivitiesOfThree();
            ActivityListController controller = (ActivityListController) load("ActivityList.fxml");
            controller.setManager(manager);

            TableView<Activity> table = field(controller, "activityTable");
            TextField filter = field(controller, "filterField");

            filter.setText("bob");
            invokeHandler(controller, "handleFilter");
            assertEquals(1, table.getItems().size());

            table.getSelectionModel().select(0);
            invokeHandler(controller, "handleJoin");

            assertEquals(1, table.getItems().size(), "the filter was reset by the join");
            assertNotNull(table.getSelectionModel().getSelectedItem());
        });
    }

    @Test
    @DisplayName("the create form offers exactly the enum values, and no typed-in dates")
    void createFormIsDrivenByTheEnums() throws Exception {
        FxToolkit.onFxThread(() -> {
            Object controller = load("CreateInvitation.fxml");

            ComboBox<Location> locationBox = field(controller, "locationBox");
            ComboBox<Gender> genderBox = field(controller, "genderBox");
            DatePicker datePicker = field(controller, "datePicker");

            assertEquals(Location.values().length, locationBox.getItems().size());
            assertEquals(Location.MARINO_RECREATION_CENTER, locationBox.getItems().get(0));
            assertEquals("Marino Recreation Center", locationBox.getItems().get(0).toString());
            assertEquals(Gender.values().length, genderBox.getItems().size());

            // The day cell factory only greys out cells in the popup; an editable
            // text field would still take a typed-in past date.
            assertTrue(!datePicker.isEditable(), "a past date could be typed into the DatePicker");
        });
    }

    @Test
    @DisplayName("edit mode pre-fills from the invitation, with no time-slot string to re-parse")
    void editModePrefillsFromTheInvitation() throws Exception {
        FxToolkit.onFxThread(() -> {
            InvitationManager manager = threeActivitiesOfThree();
            Invitation target = manager.getInvitationList().get(0);

            CreateInvitationController controller =
                    (CreateInvitationController) load("CreateInvitation.fxml");
            controller.setManager(manager);
            controller.setEditActivity(Activity.of(target));

            ComboBox<Location> locationBox = field(controller, "locationBox");
            ComboBox<Gender> genderBox = field(controller, "genderBox");

            assertEquals(target.getOrganizer(), ((TextField) field(controller, "orgField")).getText());
            assertEquals(target.getSport(), ((TextField) field(controller, "sportField")).getText());
            assertSame(target.getLocation(), locationBox.getValue());
            assertSame(target.getGender(), genderBox.getValue());
            assertEquals("3", ((TextField) field(controller, "countField")).getText());
            assertEquals("09", ((TextField) field(controller, "startHField")).getText());
            assertEquals("00", ((TextField) field(controller, "startMField")).getText());
            assertEquals("10", ((TextField) field(controller, "endHField")).getText());
            assertEquals("Update", ((Button) field(controller, "saveButton")).getText());
        });
    }
}
