package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class CreateInvitationController {

    private InvitationManager manager; // Shared data manager passed in from the previous screen
    private boolean editMode = false; // Editing an existing activity (true) or creating a new one (false)
    private Invitation editingInvitation = null; // The invitation being edited
    private Runnable onSaveSuccess; // Optional callback

    // FXML fields injected from CreateInvitation.fxml
    @FXML private TextField orgField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startHField;
    @FXML private TextField startMField;
    @FXML private TextField endHField;
    @FXML private TextField endMField;
    @FXML private TextField countField;
    @FXML private TextField sportField;
    @FXML private ComboBox<Location> locationBox;
    @FXML private ComboBox<Gender> genderBox;
    @FXML private Button saveButton;

    // Populate the combo boxes from the enums, so the form can never offer a
    // location the rest of the app does not know about.
    @FXML
    public void initialize() {
        locationBox.getItems().addAll(Location.values());
        genderBox.getItems().addAll(Gender.values());

        // The day cell factory only greys out cells in the popup; the text field
        // would still accept a typed-in past date, so it is switched off and
        // handleSave checks the date again anyway.
        datePicker.setEditable(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setManager(InvitationManager manager) {
        this.manager = manager;
    }

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    /**
     * Switches the form into edit mode and fills it from the row's underlying
     * invitation. Call {@link #setManager} first. Everything is read from the
     * invitation rather than the table row, so there is no time-slot string to
     * re-parse and nothing to keep in sync.
     */
    public void setEditActivity(Activity activity) {
        if (activity == null) {
            return;
        }

        if (manager == null) {
            showError("Manager is not initialized.");
            return;
        }

        Invitation target = manager.findByPin(activity.getPin());
        if (target == null) {
            showError("Activity not found.");
            return;
        }

        this.editMode = true;
        this.editingInvitation = target;

        orgField.setText(target.getOrganizer());
        sportField.setText(target.getSport());
        locationBox.setValue(target.getLocation());
        genderBox.setValue(target.getGender());
        countField.setText(String.valueOf(target.getCount()));
        datePicker.setValue(LocalDate.parse(target.getDate()));
        startHField.setText(String.format("%02d", target.getStartH()));
        startMField.setText(String.format("%02d", target.getStartM()));
        endHField.setText(String.format("%02d", target.getEndH()));
        endMField.setText(String.format("%02d", target.getEndM()));

        saveButton.setText("Update");
    }

    // Validates the form, then hands the values to the model. Blank fields,
    // impossible times and bad player counts are all rejected by Invitation, so
    // creating and editing enforce exactly the same rules; only the name format
    // and the no-past-dates rule are checked here, because they are form rules.
    @FXML
    private void handleSave() {
        try {
            if (manager == null) {
                showError("Manager is not initialized.");
                return;
            }

            String organizer = orgField.getText();
            if (organizer == null || organizer.trim().isEmpty()) {
                showError("Please enter organizer name.");
                return;
            }
            if (!organizer.trim().matches("\\p{L}[\\p{L} .'-]*")) {
                showError("Organizer name must start with a letter and may only contain "
                        + "letters, spaces, apostrophes, hyphens and periods.");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Please select a date.");
                return;
            }
            if (date.isBefore(LocalDate.now())) {
                showError("Please select today or a later date.");
                return;
            }

            int startH = Integer.parseInt(startHField.getText().trim());
            int startM = Integer.parseInt(startMField.getText().trim());
            int endH = Integer.parseInt(endHField.getText().trim());
            int endM = Integer.parseInt(endMField.getText().trim());
            int count = Integer.parseInt(countField.getText().trim());

            String sport = sportField.getText();
            Location location = locationBox.getValue();
            Gender gender = genderBox.getValue();

            if (!editMode) {
                Invitation invitation = new Invitation(organizer, date.toString(), startH, startM, endH, endM, count,
                        sport, location, gender);

                manager.addInvitation(invitation);
                showInfo("Reservation Successful!", "Your PIN: " + invitation.getPin());

            } else {
                // The PIN encodes location and gender, so an edit can change it.
                String pin = manager.updateInvitation(editingInvitation, organizer, date.toString(), startH, startM,
                        endH, endM, count, sport, location, gender);

                showInfo("Update Successful!", "Activity updated.\nYour PIN is now: " + pin);
            }

            if (onSaveSuccess != null) {
                onSaveSuccess.run();
            }

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Please enter whole numbers for the times and the player count.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            // Anything reaching here is a defect, so keep the trace.
            e.printStackTrace();
            showError("Unexpected error: " + e);
        }
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
