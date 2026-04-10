package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class CreateInvitationController {

    private InvitationManager manager;
    private boolean editMode = false;
    private Activity editingActivity = null;

    @FXML private TextField orgField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startHField;
    @FXML private TextField startMField;
    @FXML private TextField endHField;
    @FXML private TextField endMField;
    @FXML private TextField countField;
    @FXML private TextField sportField;
    @FXML private ComboBox<String> locationBox;
    @FXML private ComboBox<String> genderBox;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        locationBox.getItems().addAll(
            "Marino Recreation Center",
            "Cabot Center",
            "Carter Playground",
            "SquashBusters",
            "Roxbury YMCA"
        );

        genderBox.getItems().addAll(
            "Male",
            "Female",
            "All Gender"
        );
    }

    public void setManager(InvitationManager manager) {
        this.manager = manager;
    }

    public void setEditActivity(Activity activity) {
        if (activity == null) return;

        this.editMode = true;
        this.editingActivity = activity;

        sportField.setText(activity.getActivityName());
        orgField.setText(activity.getOrganizer());
        locationBox.setValue(activity.getLocation());

        if (activity.getDate() != null && !activity.getDate().isEmpty()) {
            try {
                datePicker.setValue(LocalDate.parse(activity.getDate()));
            } catch (Exception e) {
                // ignore
            }
        }

        if (activity.getTimeSlot() != null && activity.getTimeSlot().contains("~")) {
            try {
                String[] parts = activity.getTimeSlot().split("~");
                String[] start = parts[0].trim().split(":");
                String[] end = parts[1].trim().split(":");

                startHField.setText(start[0]);
                startMField.setText(start[1]);
                endHField.setText(end[0]);
                endMField.setText(end[1]);
            } catch (Exception e) {
                // ignore
            }
        }
    }

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
            if (!organizer.matches("[a-zA-Z ]+")) {
                showError("Organizer name must contain letters only.");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Please select a date.");
                return;
            }

            int startH = Integer.parseInt(startHField.getText());
            int startM = Integer.parseInt(startMField.getText());
            int endH = Integer.parseInt(endHField.getText());
            int endM = Integer.parseInt(endMField.getText());
            int count = Integer.parseInt(countField.getText());

            String sport = sportField.getText();
            String location = locationBox.getValue();
            String gender = genderBox.getValue();

            if (sport == null || sport.trim().isEmpty()) {
                showError("Please enter sport name.");
                return;
            }

            if (location == null || location.trim().isEmpty()) {
                showError("Please select a location.");
                return;
            }

            if (gender == null || gender.trim().isEmpty()) {
                showError("Please select gender.");
                return;
            }

            if (!editMode) {
                // add mode
                Invitation invitation = new Invitation(
                    organizer,
                    date.toString(),
                    startH,
                    startM,
                    endH,
                    endM,
                    count,
                    sport,
                    location,
                    gender
                );

                manager.addInvitation(invitation);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Reservation Successful!");
                alert.setContentText("Your PIN: " + invitation.getPin());
                alert.showAndWait();

            } else {
                // edit mode: 先改畫面上的 Activity
                editingActivity.setActivityName(sport);
                editingActivity.setOrganizer(organizer);
                editingActivity.setDate(date.toString());
                editingActivity.setLocation(location);
                editingActivity.setTimeSlot(
                    String.format("%02d:%02d ~ %02d:%02d", startH, startM, endH, endM)
                );

                // 再改 manager 裡真正那筆 Invitation
                Invitation target = manager.findByPin(editingActivity.getPin());

                if (target != null) {
//                    target.setOrganizer(organizer);
                    target.setDate(date.toString());
                    target.setStartH(startH);
                    target.setStartM(startM);
                    target.setEndH(endH);
                    target.setEndM(endM);
                    target.setCount(count);
                    target.setSport(sport);
                    target.setLocation(location);
                    target.setGender(gender);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Update Successful!");
                alert.setContentText("Activity updated successfully.");
                alert.showAndWait();
            }

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Unexpected error occurred.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}