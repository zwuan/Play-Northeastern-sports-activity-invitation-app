package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class CreateInvitationController {
    
	
    private InvitationManager manager; // Shared data manager passed in from the previous screen
    private boolean editMode = false; // Tracks whether if we are editing an existing activity (true) or creating a new one (false)
    private Activity editingActivity = null; // The activity being edited
    private Runnable onSaveSuccess; //Optional Callback
    
    //FXML fields injected from CreateInvitation.fxml 
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
    
    //populate Combo Box for location and gender, also setting datePicker
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
        
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));// can only choose date today or date after today
            }
        });
    }
    
    //set Manager
    public void setManager(InvitationManager manager) {
        this.manager = manager;
    }
    
    //set Callback
    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }
    
    public void setEditActivity(Activity activity) {
        if (activity == null) return;
        
        //pre-fill data
        this.editMode = true;
        this.editingActivity = activity;
        
        sportField.setText(activity.getActivityName());
        orgField.setText(activity.getOrganizer());
        locationBox.setValue(activity.getLocation());
        
        // Parse and set the date, show error if format is invalid
        if (activity.getDate() != null && !activity.getDate().isEmpty()) {
            try {
                datePicker.setValue(LocalDate.parse(activity.getDate()));
            } catch (Exception e) {
            	showError("Invalid Date.");
            }
        }
       
        // Parse time slot (format: "HH:MM ~ HH:MM") and fill into separate fields
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
            	showError("Invalid Time.");
            }
        }
        
     // Find the matching Invitation in the manager to load count and gender
        if (manager != null) {
            Invitation target = manager.findByPin(activity.getPin());
            if (target != null) {
                countField.setText(String.valueOf(target.getCount()));
                genderBox.setValue(target.getGender());
            }
        }
        
      //display update message
        saveButton.setText("Update");
    }
    
    //Handle Save Button and Validates all input fields
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

                if (onSaveSuccess != null) {
                    onSaveSuccess.run();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Reservation Successful!");
                alert.setContentText("Your PIN: " + invitation.getPin());
                alert.showAndWait();

            } else {
                //update the existing Activity and Invitation in place
                editingActivity.setActivityName(sport);
                editingActivity.setOrganizer(organizer);
                editingActivity.setDate(date.toString());
                editingActivity.setLocation(location);
                editingActivity.setTimeSlot(
                    String.format("%02d:%02d ~ %02d:%02d", startH, startM, endH, endM)
                );
                
              //update the underlying Invitation object in the manager
                Invitation target = manager.findByPin(editingActivity.getPin());
                
                if (target != null) {
                    // target.setOrganizer(organizer);
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
